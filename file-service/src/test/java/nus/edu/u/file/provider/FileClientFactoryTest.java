package nus.edu.u.file.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FileClientFactoryTest {

    private final GcsFileClient gcsClient = org.mockito.Mockito.mock(GcsFileClient.class);
    private final FileClientFactory factory = new FileClientFactory(gcsClient);

    @Test
    void create_withGcsProvider_returnsGcsClient() {
        FileClient client = factory.create(" gCs ");
        assertThat(client).isSameAs(gcsClient);
    }

    @Test
    void create_withBlankProvider_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> factory.create(" "));
    }

    @Test
    void create_withUnsupportedProvider_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> factory.create("s3"));
    }
}
