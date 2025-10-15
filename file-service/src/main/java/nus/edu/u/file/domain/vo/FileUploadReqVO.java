package nus.edu.u.file.domain.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadReqVO {

    @NotNull(message = "taskLogId is required")
    private Long taskLogId;

    @NotNull(message = "eventId is required")
    private Long eventId;

    @NotEmpty(message = "files must not be empty")
    private List<MultipartFile> files;
}
