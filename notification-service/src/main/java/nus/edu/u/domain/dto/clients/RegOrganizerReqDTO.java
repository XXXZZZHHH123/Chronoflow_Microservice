package nus.edu.u.domain.dto.clients;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegOrganizerReqDTO {

    private String name;
    private String username;
    private String userEmail;
    private String mobile;
    private String organizationName;
    private String organizationAddress;
    private String organizationCode;
}
