package nus.edu.u.domain.dto.clients;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegSearchReqDTO {

    private Long organizationId;
    private Long userId;
    private String recipientEmail;
}
