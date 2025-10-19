package nus.edu.u.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import java.time.LocalDateTime;
import java.util.List;
import nus.edu.u.file.config.FileProviderPropertiesConfig;
import nus.edu.u.file.domain.dataobject.FileDO;
import nus.edu.u.file.domain.vo.FileResultVO;
import nus.edu.u.file.domain.vo.FileUploadReqVO;
import nus.edu.u.file.mapper.FileMapper;
import nus.edu.u.file.provider.FileClient;
import nus.edu.u.file.provider.FileClientFactory;
import nus.edu.u.file.provider.GcsFileClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceImplTest {

    @Mock private FileClientFactory fileClientFactory;
    @Mock private FileProviderPropertiesConfig providerConfig;
    @Mock private FileMapper fileMapper;
    @Mock private GcsFileClient gcsClient;

    @InjectMocks private FileStorageServiceImpl service;

    @AfterEach
    void tearDownContext() {
        try {
            if (StpUtil.isLogin()) {
                StpUtil.logout();
            }
        } catch (Exception ignored) {
            // ignore missing context during tests
        }
        SaTokenContextMockUtil.clearContext();
    }

    @Test
    void uploadToTaskLog_successfullyUploadsAndPersistsMetadata() {
        when(providerConfig.getProvider()).thenReturn("gcs");
        when(fileClientFactory.create("gcs")).thenReturn(gcsClient);
        when(gcsClient.uploadFile(any()))
                .thenAnswer(
                        invocation -> {
                            MockMultipartFile file = invocation.getArgument(0);
                            return new FileClient.FileUploadResult(
                                    "obj-" + file.getOriginalFilename(),
                                    file.getContentType(),
                                    file.getSize(),
                                    null);
                        });
        when(gcsClient.generateSignedUrl(any()))
                .thenAnswer(invocation -> "https://signed/" + invocation.getArgument(0));

        FileUploadReqVO req = new FileUploadReqVO();
        req.setTaskLogId(10L);
        req.setEventId(50L);
        req.setFiles(
                List.of(
                        new MockMultipartFile("file1", "a.txt", "text/plain", "alpha".getBytes()),
                        new MockMultipartFile("file2", "b.txt", "text/csv", "beta".getBytes())));

        SaTokenContextMockUtil.setMockContext();
        StpUtil.login(123L);

        List<FileResultVO> results = service.uploadToTaskLog(req);

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(FileResultVO::getObjectName)
                .containsExactlyInAnyOrder("obj-a.txt", "obj-b.txt");
        assertThat(results).allMatch(r -> r.getSignedUrl().startsWith("https://signed/"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FileDO>> batchCaptor =
                (ArgumentCaptor) ArgumentCaptor.forClass(List.class);
        verify(fileMapper).insertBatch(batchCaptor.capture());
        List<FileDO> persisted = batchCaptor.getValue();
        assertThat(persisted).hasSize(2);
        assertThat(persisted)
                .allSatisfy(
                        entity -> {
                            assertThat(entity.getTaskLogId()).isEqualTo(10L);
                            assertThat(entity.getEventId()).isEqualTo(50L);
                            assertThat(entity.getProvider()).isEqualTo("gcs");
                            assertThat(entity.getCreator()).isEqualTo("123");
                            assertThat(entity.getUpdater()).isEqualTo("123");
                            assertThat(entity.getCreateTime()).isNotNull();
                            assertThat(entity.getUpdateTime()).isNotNull();
                        });
        verify(gcsClient, times(2)).generateSignedUrl(any());
    }

    @Test
    void uploadToTaskLog_whenFilesEmpty_throwsException() {
        FileUploadReqVO req = new FileUploadReqVO();
        req.setTaskLogId(1L);
        req.setEventId(2L);
        req.setFiles(List.of());

        assertThrows(IllegalArgumentException.class, () -> service.uploadToTaskLog(req));
        verify(fileMapper, never()).insertBatch(any());
    }

    @Test
    void uploadToTaskLog_onFailureRollsBackUploadedObjects() {
        when(providerConfig.getProvider()).thenReturn("gcs");
        when(fileClientFactory.create("gcs")).thenReturn(gcsClient);

        when(gcsClient.uploadFile(any()))
                .thenAnswer(
                        invocation -> {
                            MockMultipartFile file = invocation.getArgument(0);
                            if ("good.txt".equals(file.getOriginalFilename())) {
                                return new FileClient.FileUploadResult(
                                        "obj-" + file.getOriginalFilename(),
                                        file.getContentType(),
                                        file.getSize(),
                                        null);
                            }
                            throw new RuntimeException("upload failure");
                        });

        FileUploadReqVO req = new FileUploadReqVO();
        req.setTaskLogId(9L);
        req.setEventId(3L);
        req.setFiles(
                List.of(
                        new MockMultipartFile("file1", "good.txt", "text/plain", "ok".getBytes()),
                        new MockMultipartFile(
                                "file2", "bad.txt", "text/plain", "boom".getBytes())));

        SaTokenContextMockUtil.setMockContext();
        StpUtil.login(999L);

        RuntimeException ex =
                assertThrows(RuntimeException.class, () -> service.uploadToTaskLog(req));
        assertThat(ex).hasMessageContaining("upload failure");

        verify(fileMapper, never()).insertBatch(any());
        verify(gcsClient).deleteQuietly("obj-good.txt");
    }

    @Test
    void downloadFile_returnsSignedUrlForGcs() {
        FileDO file =
                FileDO.builder()
                        .taskLogId(1L)
                        .provider("gcs")
                        .objectName("obj")
                        .name("file.txt")
                        .type("text/plain")
                        .size(123L)
                        .build();
        file.setCreateTime(LocalDateTime.now());
        file.setUpdateTime(LocalDateTime.now());

        when(fileMapper.selectOne(any())).thenReturn(file);
        when(fileClientFactory.create("gcs")).thenReturn(gcsClient);
        when(gcsClient.generateSignedUrl("obj")).thenReturn("https://signed/obj");

        FileResultVO result = service.downloadFile(1L);

        assertThat(result.getObjectName()).isEqualTo("obj");
        assertThat(result.getSignedUrl()).isEqualTo("https://signed/obj");
        verify(gcsClient).generateSignedUrl("obj");
    }

    @Test
    void downloadFile_whenNotFound_throwsException() {
        when(fileMapper.selectOne(any())).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.downloadFile(1L));
    }

    @Test
    void downloadFile_whenProviderUnsupported_throwsException() {
        FileDO file =
                FileDO.builder()
                        .taskLogId(1L)
                        .provider("s3")
                        .objectName("obj")
                        .name("file.txt")
                        .type("text/plain")
                        .size(10L)
                        .build();
        when(fileMapper.selectOne(any())).thenReturn(file);
        when(fileClientFactory.create("s3"))
                .thenReturn(
                        new FileClient() {
                            @Override
                            public FileUploadResult uploadFile(
                                    org.springframework.web.multipart.MultipartFile file) {
                                return null;
                            }
                        });

        assertThrows(UnsupportedOperationException.class, () -> service.downloadFile(1L));
    }

    @Test
    void downloadFilesByTaskLogId_returnsEmptyWhenNoFiles() {
        when(fileMapper.selectList(any())).thenReturn(List.of());

        assertThat(service.downloadFilesByTaskLogId(10L)).isEmpty();
    }

    @Test
    void downloadFilesByTaskLogId_returnsSignedUrls() {
        FileDO file1 =
                FileDO.builder()
                        .taskLogId(1L)
                        .provider("gcs")
                        .objectName("obj1")
                        .name("file1.txt")
                        .type("text/plain")
                        .size(11L)
                        .build();
        FileDO file2 =
                FileDO.builder()
                        .taskLogId(1L)
                        .provider("gcs")
                        .objectName("obj2")
                        .name("file2.txt")
                        .type("text/plain")
                        .size(22L)
                        .build();

        when(fileMapper.selectList(any())).thenReturn(List.of(file1, file2));
        when(fileClientFactory.create("gcs")).thenReturn(gcsClient);
        when(gcsClient.generateSignedUrl("obj1")).thenReturn("url1");
        when(gcsClient.generateSignedUrl("obj2")).thenReturn("url2");

        List<FileResultVO> results = service.downloadFilesByTaskLogId(1L);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(FileResultVO::getSignedUrl).containsExactly("url1", "url2");
    }

    @Test
    void downloadFilesByTaskLogId_whenProviderUnsupported_throwsException() {
        FileDO file =
                FileDO.builder()
                        .taskLogId(1L)
                        .provider("s3")
                        .objectName("obj")
                        .name("file.txt")
                        .type("text/plain")
                        .size(15L)
                        .build();
        when(fileMapper.selectList(any())).thenReturn(List.of(file));
        when(fileClientFactory.create("s3"))
                .thenReturn(
                        new FileClient() {
                            @Override
                            public FileUploadResult uploadFile(
                                    org.springframework.web.multipart.MultipartFile file) {
                                return null;
                            }
                        });

        assertThrows(
                UnsupportedOperationException.class, () -> service.downloadFilesByTaskLogId(1L));
    }
}
