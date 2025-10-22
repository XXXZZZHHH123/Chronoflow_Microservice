package nus.edu.u.file.convert;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import nus.edu.u.file.domain.vo.FileUploadReqVO;
import nus.edu.u.shared.rpc.file.FileUploadReqDTO;
import nus.edu.u.shared.rpc.file.FileUploadReqDTO.FileResource;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.web.multipart.MultipartFile;

class FileRpcConvertTest {

    private final FileRpcConvert mapper = Mappers.getMapper(FileRpcConvert.class);

    @Test
    void toDomain_whenRequestNull_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_convertsResourcesToMultipartFiles() throws Exception {
        byte[] content = "hello-world".getBytes(StandardCharsets.UTF_8);
        FileResource resource =
                FileResource.builder()
                        .name("note.txt")
                        .contentType("text/plain")
                        .content(content)
                        .size((long) content.length)
                        .build();
        List<FileResource> resources = new ArrayList<>();
        resources.add(resource);
        resources.add(null);
        FileUploadReqDTO dto =
                FileUploadReqDTO.builder().taskLogId(5L).eventId(6L).files(resources).build();

        FileUploadReqVO domain = mapper.toDomain(dto);

        assertThat(domain.getTaskLogId()).isEqualTo(5L);
        assertThat(domain.getEventId()).isEqualTo(6L);
        assertThat(domain.getFiles()).hasSize(1);
        MultipartFile multipartFile = domain.getFiles().get(0);
        assertThat(multipartFile.getOriginalFilename()).isEqualTo("note.txt");
        assertThat(multipartFile.getContentType()).isEqualTo("text/plain");
        assertThat(multipartFile.getSize()).isEqualTo(content.length);
        assertThat(multipartFile.isEmpty()).isFalse();
        assertThat(multipartFile.getBytes()).isEqualTo(content);

        Path tempFile = Files.createTempFile("upload", ".txt");
        try {
            multipartFile.transferTo(tempFile.toFile());
            assertThat(Files.readAllBytes(tempFile)).isEqualTo(content);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
