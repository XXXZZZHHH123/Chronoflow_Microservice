package nus.edu.u.task.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AttachmentDTO(
        @JsonProperty("filename") String filename,
        @JsonProperty("contentType") String contentType,
        @JsonProperty("bytes") byte[] bytes,
        @JsonProperty("url") String url,
        @JsonProperty("inline") boolean inline,
        @JsonProperty("contentId") String contentId) {}
