package nus.edu.u.domain.dto.email;

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

    /** Organization ID that the member is invited to */
    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    /** Platform user ID of the member being invited */
    @NotNull(message = "Member ID is required")
    private Long userId;

    /** Recipient email address of the invited member */
    @Email(message = "Please provide a valid email address")
    private String recipientEmail;
}
