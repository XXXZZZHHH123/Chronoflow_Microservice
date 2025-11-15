package nus.edu.u.domain.dto.common;

public record AttachmentDTO(
        String filename,
        String contentType,
        byte[] bytes,
        String url,
        boolean inline,
        String contentId) {}
