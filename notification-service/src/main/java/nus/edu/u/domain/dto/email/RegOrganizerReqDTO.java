package nus.edu.u.domain.dto.email;

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

    @NotEmpty(message = "Organizer name is required")
    @Size(max = 100, message = "Organizer name must not exceed 100 characters")
    private String name;

    @NotEmpty(message = "Username is required")
    @Size(min = 6, max = 100, message = "Username must be between 6 and 100 characters")
    private String username;

    @NotEmpty(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String userEmail;

    @NotEmpty(message = "Mobile number is required")
    private String mobile;

    @NotEmpty(message = "Organization name is required")
    @Size(max = 100, message = "Organization name must not exceed 100 characters")
    private String organizationName;

    @Size(max = 500, message = "Organization address must not exceed 500 characters")
    private String organizationAddress;

    @Size(min = 6, max = 20, message = "Organization code must be between 6 and 20 characters")
    private String organizationCode;
}
