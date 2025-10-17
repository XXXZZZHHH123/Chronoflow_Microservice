package nus.edu.u.shared.rpc.file;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResultDTO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private String objectName;

    private String name;

    private String contentType;

    private long size;

    private String signedUrl;
}
