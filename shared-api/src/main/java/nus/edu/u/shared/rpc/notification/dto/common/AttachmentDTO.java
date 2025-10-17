package nus.edu.u.shared.rpc.notification.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AttachmentDTO(
        @JsonProperty("filename") String filename,
        @JsonProperty("contentType") String contentType,
        @JsonProperty("bytes") byte[] bytes,
        @JsonProperty("url") String url,
        @JsonProperty("inline") boolean inline,
        @JsonProperty("contentId") String contentId
) {}