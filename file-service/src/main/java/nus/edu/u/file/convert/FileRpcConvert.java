package nus.edu.u.file.convert;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import nus.edu.u.shared.rpc.file.FileResultDTO;
import nus.edu.u.shared.rpc.file.FileUploadReqDTO;
import nus.edu.u.shared.rpc.file.FileUploadReqDTO.FileResource;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.web.multipart.MultipartFile;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface FileRpcConvert {

    FileResultDTO toRpc(nus.edu.u.file.domain.vo.FileResultVO bean);

    List<FileResultDTO> toRpcList(List<nus.edu.u.file.domain.vo.FileResultVO> list);

    default nus.edu.u.file.domain.vo.FileUploadReqVO toDomain(FileUploadReqDTO req) {
        if (req == null) {
            return null;
        }
        nus.edu.u.file.domain.vo.FileUploadReqVO domain =
                new nus.edu.u.file.domain.vo.FileUploadReqVO();
        domain.setTaskLogId(req.getTaskLogId());
        domain.setEventId(req.getEventId());
        domain.setFiles(toMultipartFiles(req.getFiles()));
        return domain;
    }

    private List<MultipartFile> toMultipartFiles(List<FileResource> resources) {
        if (resources == null || resources.isEmpty()) {
            return List.of();
        }
        List<MultipartFile> files = new ArrayList<>(resources.size());
        for (FileResource resource : resources) {
            if (resource == null) {
                continue;
            }
            files.add(
                    new ByteArrayMultipartFile(
                            resource.getName(),
                            resource.getContentType(),
                            resource.getContent(),
                            resource.getSize()));
        }
        return files;
    }

    final class ByteArrayMultipartFile implements MultipartFile {

        private final String name;
        private final String contentType;
        private final byte[] content;
        private final long size;

        ByteArrayMultipartFile(String name, String contentType, byte[] content, Long declaredSize) {
            this.name = name;
            this.contentType = contentType;
            this.content = content == null ? new byte[0] : content;
            this.size = declaredSize != null ? declaredSize : this.content.length;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public byte[] getBytes() {
            return content.clone();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }
}
