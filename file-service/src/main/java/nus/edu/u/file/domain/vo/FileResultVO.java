package nus.edu.u.file.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResultVO {

    private String objectName;

    private String name;

    private String contentType;

    private long size;

    private String signedUrl;
}
