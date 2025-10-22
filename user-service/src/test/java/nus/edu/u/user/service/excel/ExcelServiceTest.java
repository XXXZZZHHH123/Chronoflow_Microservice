package nus.edu.u.user.service.excel;

import static nus.edu.u.common.enums.ErrorCodeConstants.ROLE_NOT_FOUND;
import static nus.edu.u.common.exception.enums.GlobalErrorCodeConstants.EXCEL_FORMAT_ERROR;
import static nus.edu.u.common.exception.enums.GlobalErrorCodeConstants.EXCEL_HEADER_MISSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.user.domain.dataobject.role.RoleDO;
import nus.edu.u.user.domain.dto.CreateUserDTO;
import nus.edu.u.user.mapper.role.RoleMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ExcelServiceTest {

    @Mock private RoleMapper roleMapper;

    @InjectMocks private ExcelService excelService;

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, RoleDO.class);
    }

    @Test
    void parseCreateOrUpdateRows_whenFileNullOrEmpty_returnsEmptyList() throws IOException {
        assertThat(excelService.parseCreateOrUpdateRows(null)).isEmpty();

        MockMultipartFile empty = new MockMultipartFile("file", new byte[0]);
        assertThat(excelService.parseCreateOrUpdateRows(empty)).isEmpty();
    }

    @Test
    void parseCreateOrUpdateRows_parsesRowsAndTrimsContent() throws IOException {
        MockMultipartFile file =
                excelMockFile(
                        List.of(
                                List.of("email", "roleKeys", "remark"),
                                List.of(" alice@example.com ", "admin，member admin", " leader "),
                                List.of("bob@example.com", "member", "")));

        when(roleMapper.selectList(any()))
                .thenReturn(
                        List.of(
                                RoleDO.builder().id(10L).roleKey("admin").build(),
                                RoleDO.builder().id(20L).roleKey("member").build()));

        List<CreateUserDTO> rows = excelService.parseCreateOrUpdateRows(file);

        assertThat(rows).hasSize(2);
        CreateUserDTO first = rows.get(0);
        assertThat(first.getEmail()).isEqualTo("alice@example.com");
        assertThat(first.getRoleIds()).containsExactly(10L, 20L);
        assertThat(first.getRemark()).isEqualTo("leader");
        assertThat(first.getRowIndex()).isEqualTo(2);

        CreateUserDTO second = rows.get(1);
        assertThat(second.getRoleIds()).containsExactly(20L);
        assertThat(second.getRemark()).isEmpty();
        assertThat(second.getRowIndex()).isEqualTo(3);

        verify(roleMapper, times(1)).selectList(any());
    }

    @Test
    void parseCreateOrUpdateRows_whenNewRoleAppears_fetchesAdditionalIds() throws IOException {
        MockMultipartFile file =
                excelMockFile(
                        List.of(
                                List.of("email", "roleKeys"),
                                List.of("alice@example.com", "admin"),
                                List.of("bob@example.com", "staff")));

        when(roleMapper.selectList(any()))
                .thenReturn(
                        List.of(RoleDO.builder().id(10L).roleKey("admin").build()),
                        List.of(RoleDO.builder().id(30L).roleKey("staff").build()));

        List<CreateUserDTO> rows = excelService.parseCreateOrUpdateRows(file);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(1).getRoleIds()).containsExactly(30L);
        verify(roleMapper, times(2)).selectList(any());
    }

    @Test
    void parseCreateOrUpdateRows_whenRolesCellEmpty_returnsEmptyRoleIds() throws IOException {
        MockMultipartFile file =
                excelMockFile(
                        List.of(
                                List.of("email", "roleKeys", "remark"),
                                List.of("carol@example.com", "", "")));

        List<CreateUserDTO> rows = excelService.parseCreateOrUpdateRows(file);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getRoleIds()).isEmpty();
        assertThat(rows.get(0).getRemark()).isEmpty();
        verify(roleMapper, never()).selectList(any());
    }

    @Test
    void parseCreateOrUpdateRows_whenHeaderMissing_throwsServiceException() throws IOException {
        MockMultipartFile file =
                excelMockFile(
                        List.of(
                                List.of("username", "roleKeys"),
                                List.of("dave@example.com", "admin")));

        assertThatThrownBy(() -> excelService.parseCreateOrUpdateRows(file))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(EXCEL_HEADER_MISSING.getCode());
    }

    @Test
    void parseCreateOrUpdateRows_whenUnknownRole_throwsServiceException() throws IOException {
        MockMultipartFile file =
                excelMockFile(
                        List.of(
                                List.of("email", "roleKeys"),
                                List.of("eve@example.com", "ghost-role")));

        when(roleMapper.selectList(any())).thenReturn(List.of());

        assertThatThrownBy(() -> excelService.parseCreateOrUpdateRows(file))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(ROLE_NOT_FOUND.getCode());
    }

    @Test
    void parseCreateOrUpdateRows_whenFileCorrupted_throwsExcelFormatError() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getInputStream()).thenThrow(new IllegalArgumentException("boom"));

        assertThatThrownBy(() -> excelService.parseCreateOrUpdateRows(file))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(EXCEL_FORMAT_ERROR.getCode());
    }

    private MockMultipartFile excelMockFile(List<List<String>> rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Sheet1");
            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r);
                List<String> data = rows.get(r);
                for (int c = 0; c < data.size(); c++) {
                    row.createCell(c).setCellValue(data.get(c));
                }
            }
            workbook.write(out);
            byte[] bytes = out.toByteArray();
            return new MockMultipartFile(
                    "file",
                    "users.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    bytes);
        }
    }
}
