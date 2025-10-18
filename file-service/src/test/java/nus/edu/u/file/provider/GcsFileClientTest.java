package nus.edu.u.file.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import nus.edu.u.file.config.GcsPropertiesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class GcsFileClientTest {

    @Mock private Storage storage;

    private GcsPropertiesConfig config;
    private GcsFileClient client;

    @BeforeEach
    void setUp() {
        config = new GcsPropertiesConfig();
        config.setBucket("bucket");
        config.setSignedUrlExpiryMinutes(15L);
        client = new GcsFileClient(storage, config);
    }

    @Test
    void uploadFile_uploadsToStorage() throws IOException {
        MockMultipartFile file =
                new MockMultipartFile("file", "hello.txt", "text/plain", "hello-world".getBytes());
        when(storage.createFrom(any(BlobInfo.class), any(java.io.InputStream.class)))
                .thenReturn((Blob) null);

        FileClient.FileUploadResult result = client.uploadFile(file);

        assertThat(result.objectName()).contains("hello.txt");
        assertThat(result.contentType()).isEqualTo("text/plain");
        assertThat(result.size()).isEqualTo(file.getSize());

        ArgumentCaptor<BlobInfo> infoCaptor = ArgumentCaptor.forClass(BlobInfo.class);
        verify(storage).createFrom(infoCaptor.capture(), any(java.io.InputStream.class));
        assertThat(infoCaptor.getValue().getBucket()).isEqualTo("bucket");
    }

    @Test
    void uploadFile_whenFileNull_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> client.uploadFile(null));
    }

    @Test
    void uploadFile_whenStorageFails_wrapsIOException() throws IOException {
        MockMultipartFile file =
                new MockMultipartFile("file", "bad.txt", "text/plain", "boom".getBytes());
        doThrow(new IOException("fail"))
                .when(storage)
                .createFrom(any(BlobInfo.class), any(java.io.InputStream.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> client.uploadFile(file));
        assertThat(ex).hasMessageContaining("Failed to upload file to GCS");
    }

    @Test
    void generateSignedUrl_returnsUrlFromStorage() throws Exception {
        URL signed = new URL("https://gcs.example/object");
        when(storage.signUrl(
                        any(BlobInfo.class),
                        eq(15L),
                        eq(TimeUnit.MINUTES),
                        any(Storage.SignUrlOption.class)))
                .thenReturn(signed);

        String url = client.generateSignedUrl("objectName");

        assertThat(url).isEqualTo("https://gcs.example/object");
    }

    @Test
    void deleteQuietly_swallowExceptions() {
        doThrow(new RuntimeException("delete failed")).when(storage).delete(any(BlobId.class));

        client.deleteQuietly("objectName");

        verify(storage).delete(any(BlobId.class));
    }
}
