package nus.edu.u.domain.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {

    /** File name (for downloadable attachments) */
    @JsonProperty("filename")
    private String filename;

    /** MIME type, e.g. image/png or application/pdf */
    @JsonProperty("contentType")
    private String contentType;

    /** Raw bytes (inline or attached data) */
    @JsonProperty("bytes")
    private byte[] bytes;

    /** Optional external URL if resource is remote */
    @JsonProperty("url")
    private String url;

    /** True if should appear inline via CID */
    @JsonProperty("inline")
    private boolean inline;

    /** Content ID for inline <img src="cid:..."> references */
    @JsonProperty("contentId")
    private String contentId;
}
