package nus.edu.u.domain.dto.common;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenderedTemplateDTO {
    // Email
    private String subject; // from variables["subject"] or default
    private String html; // rendered HTML
    private String text; // optional plain text (variables["text"])

    // Push / WS
    private String title; // variables["title"] or default
    private String body; // rendered TXT (push/ws) or null

    @Builder.Default
    private Map<String, Object> extras = Map.of(); // variables["extras"] if provided

    // For all channels
    @Builder.Default private List<AttachmentDTO> attachments = List.of();
}
