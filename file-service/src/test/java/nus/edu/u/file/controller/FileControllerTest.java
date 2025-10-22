package nus.edu.u.file.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import nus.edu.u.file.domain.vo.FileResultVO;
import nus.edu.u.file.domain.vo.FileUploadReqVO;
import nus.edu.u.file.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock private FileStorageService fileStorageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FileController controller = new FileController(fileStorageService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setValidator(validator).build();
    }

    @Test
    void uploadToTaskLog_returnsServiceResponse() throws Exception {
        List<FileResultVO> serviceResponse =
                List.of(
                        FileResultVO.builder()
                                .objectName("obj-1")
                                .contentType("text/plain")
                                .size(4)
                                .signedUrl("https://signed/1")
                                .build());
        when(fileStorageService.uploadToTaskLog(any(FileUploadReqVO.class)))
                .thenReturn(serviceResponse);

        MockMultipartFile file =
                new MockMultipartFile("files", "note.txt", "text/plain", "data".getBytes());

        mockMvc.perform(
                        multipart("/api/files/upload")
                                .file(file)
                                .param("taskLogId", "12")
                                .param("eventId", "34")
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].objectName").value("obj-1"))
                .andExpect(jsonPath("$[0].signedUrl").value("https://signed/1"));

        ArgumentCaptor<FileUploadReqVO> captor = ArgumentCaptor.forClass(FileUploadReqVO.class);
        verify(fileStorageService).uploadToTaskLog(captor.capture());
        FileUploadReqVO captured = captor.getValue();
        assertThat(captured.getTaskLogId()).isEqualTo(12L);
        assertThat(captured.getEventId()).isEqualTo(34L);
        assertThat(captured.getFiles()).hasSize(1);
        assertThat(captured.getFiles().get(0).getOriginalFilename()).isEqualTo("note.txt");
    }

    @Test
    void uploadToTaskLog_missingRequiredFields_returnsBadRequest() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("files", "note.txt", "text/plain", "data".getBytes());

        mockMvc.perform(
                        multipart("/api/files/upload")
                                .file(file)
                                .param("taskLogId", "12")
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fileStorageService);
    }

    @Test
    void downloadFile_returnsResultFromService() throws Exception {
        FileResultVO fileResult =
                FileResultVO.builder()
                        .objectName("obj")
                        .name("report.pdf")
                        .contentType("application/pdf")
                        .size(1024)
                        .signedUrl("https://signed/report")
                        .build();
        when(fileStorageService.downloadFile(55L)).thenReturn(fileResult);

        mockMvc.perform(get("/api/files/{taskLogId}/download", 55))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objectName").value("obj"))
                .andExpect(jsonPath("$.signedUrl").value("https://signed/report"));

        verify(fileStorageService).downloadFile(55L);
    }

    @Test
    void downloadFiles_returnsListFromService() throws Exception {
        List<FileResultVO> results =
                List.of(
                        FileResultVO.builder()
                                .objectName("obj-1")
                                .signedUrl("url-1")
                                .size(10)
                                .build(),
                        FileResultVO.builder()
                                .objectName("obj-2")
                                .signedUrl("url-2")
                                .size(20)
                                .build());
        when(fileStorageService.downloadFilesByTaskLogId(66L)).thenReturn(results);

        mockMvc.perform(get("/api/files/{taskLogId}/download-all", 66))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].objectName").value("obj-1"))
                .andExpect(jsonPath("$[1].signedUrl").value("url-2"));

        verify(fileStorageService).downloadFilesByTaskLogId(66L);
    }
}
