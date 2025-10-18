package nus.edu.u.user.service.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import nus.edu.u.common.exception.ServiceException;
import nus.edu.u.user.domain.dataobject.permission.PermissionDO;
import nus.edu.u.user.domain.dataobject.role.RolePermissionDO;
import nus.edu.u.user.domain.vo.permission.PermissionReqVO;
import nus.edu.u.user.domain.vo.permission.PermissionRespVO;
import nus.edu.u.user.mapper.permission.PermissionMapper;
import nus.edu.u.user.mapper.role.RolePermissionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PermissionServiceImplTest {

    @Mock private PermissionMapper permissionMapper;
    @Mock private RolePermissionMapper rolePermissionMapper;

    @InjectMocks private PermissionServiceImpl service;

    private PermissionReqVO reqVO;

    @BeforeEach
    void setUp() {
        reqVO = new PermissionReqVO();
        reqVO.setName("View Dashboard");
        reqVO.setKey("system:view");
        reqVO.setDescription("desc");
    }

    @Test
    void listPermissions_returnsConvertedResults() {
        PermissionDO permission =
                PermissionDO.builder()
                        .id(1L)
                        .name("View")
                        .permissionKey("system:view")
                        .description("desc")
                        .build();
        when(permissionMapper.selectList(any())).thenReturn(List.of(permission));

        List<PermissionRespVO> result = service.listPermissions();

        assertThat(result)
                .singleElement()
                .satisfies(vo -> assertThat(vo.getKey()).isEqualTo("system:view"));
    }

    @Test
    void createPermission_savesEntity() {
        when(permissionMapper.insert(any(PermissionDO.class)))
                .thenAnswer(
                        invocation -> {
                            PermissionDO permission = invocation.getArgument(0);
                            permission.setId(10L);
                            return 1;
                        });

        Long id = service.createPermission(reqVO);

        assertThat(id).isEqualTo(10L);
    }

    @Test
    void createPermission_withNullRequest_throwsBadRequest() {
        assertThatThrownBy(() -> service.createPermission(null))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(
                        nus.edu.u.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST
                                .getCode());
    }

    @Test
    void getPermission_returnsConvertedValue() {
        PermissionDO permission =
                PermissionDO.builder()
                        .id(2L)
                        .name("Edit")
                        .permissionKey("system:edit")
                        .description("desc")
                        .build();
        when(permissionMapper.selectById(2L)).thenReturn(permission);

        PermissionRespVO vo = service.getPermission(2L);

        assertThat(vo.getKey()).isEqualTo("system:edit");
    }

    @Test
    void updatePermission_updatesRecord() {
        PermissionDO current =
                PermissionDO.builder()
                        .id(3L)
                        .name("Old")
                        .permissionKey("system:old")
                        .description("old")
                        .build();
        when(permissionMapper.selectById(3L)).thenReturn(current);
        when(permissionMapper.updateById(any(PermissionDO.class))).thenReturn(1);

        PermissionRespVO vo = service.updatePermission(3L, reqVO);

        assertThat(vo.getName()).isEqualTo("View Dashboard");
    }

    @Test
    void deletePermission_whenBoundToRole_throws() {
        when(rolePermissionMapper.selectList(any()))
                .thenReturn(List.of(RolePermissionDO.builder().permissionId(5L).build()));

        assertThatThrownBy(() -> service.deletePermission(5L))
                .isInstanceOf(ServiceException.class)
                .extracting("code")
                .isEqualTo(
                        nus.edu.u.common.enums.ErrorCodeConstants.CANNOT_DELETE_PERMISSION
                                .getCode());
    }

    @Test
    void deletePermission_succeeds() {
        when(rolePermissionMapper.selectList(any())).thenReturn(List.of());
        when(permissionMapper.deleteById(6L)).thenReturn(1);

        assertThat(service.deletePermission(6L)).isTrue();
    }
}
