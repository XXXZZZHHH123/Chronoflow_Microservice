package nus.edu.u.domain.dto.clients;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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
