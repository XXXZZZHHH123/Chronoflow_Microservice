package nus.edu.u.wsgateway.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WsPushRequestDTO {
    @NotBlank String userId;
    @NotBlank String eventId;
    @NotBlank String type;
    String title;
    String body;
    Map<String, Object> data;
}
