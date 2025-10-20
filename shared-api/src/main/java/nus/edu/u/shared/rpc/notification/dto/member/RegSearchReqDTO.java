package nus.edu.u.shared.rpc.notification.dto.member;

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
    private Long organizationId;

    /** Platform user ID of the member being invited */
    private Long userId;

    /** Recipient email address of the invited member */
    private String recipientEmail;
}
