package nus.edu.u.wsgateway.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WsPushRequestDTO {
    @NotBlank
    String userId;
    @NotBlank
    String eventId;
    @NotBlank
    String type;
    String title;
    String body;
    Map<String,Object> data;
}