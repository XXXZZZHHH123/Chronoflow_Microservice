package nus.edu.u.shared.rpc.file;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadReqDTO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private Long taskLogId;

    private Long eventId;

    private List<FileResource> files;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileResource implements Serializable {

        @Serial private static final long serialVersionUID = 1L;

        private String name;

        private String contentType;

        private byte[] content;

        private Long size;
    }
}
