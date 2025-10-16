package nus.edu.u.shared.rpc.file;

import java.util.List;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadReqVO {

    private Long taskLogId;

    private Long eventId;

    private List<MultipartFile> files;
}
