package nus.edu.u.shared.rpc.notification.dto.common;

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

    /** File name (used for downloadable attachments) */
    @JsonProperty("filename")
    private String filename;

    /** MIME type, e.g. image/png, application/pdf */
    @JsonProperty("contentType")
    private String contentType;

    /** Raw bytes (used for inline or attached data) */
    @JsonProperty("bytes")
    private byte[] bytes;

    /** Optional remote URL if hosted externally */
    @JsonProperty("url")
    private String url;

    /** True if to be displayed inline (<img src="cid:...">) */
    @JsonProperty("inline")
    private boolean inline;

    /** CID identifier for inline resources */
    @JsonProperty("contentId")
    private String contentId;
}
