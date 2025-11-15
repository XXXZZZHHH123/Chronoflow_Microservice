package nus.edu.u.domain.dto.template;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nus.edu.u.domain.dto.common.AttachmentDTO;

@Builder
@AllArgsConstructor
@Data
public class TemplateResponseDTO {
    String subject;
    String body;
    List<AttachmentDTO> attachments;
}
