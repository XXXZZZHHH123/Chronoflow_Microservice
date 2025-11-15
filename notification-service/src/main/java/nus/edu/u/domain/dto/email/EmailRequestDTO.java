package nus.edu.u.domain.dto.email;

import java.util.List;
import lombok.*;
import nus.edu.u.domain.dto.common.AttachmentDTO;
import nus.edu.u.enums.email.EmailProvider;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequestDTO {
    private EmailProvider provider;
    private String to;
    private String subject;
    private String html;
    private List<AttachmentDTO> attachments;
}
