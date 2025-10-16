package nus.edu.u.shared.rpc.file;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class FileUploadReqVO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @NotNull(message = "taskLogId is required")
    private Long taskLogId;

    @NotNull(message = "eventId is required")
    private Long eventId;

    @NotEmpty(message = "files must not be empty")
    private List<FileResource> files;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileResource implements Serializable {

        @Serial private static final long serialVersionUID = 1L;

        @NotNull private String name;

        private String contentType;

        @NotNull private byte[] content;

        private Long size;
    }
}
