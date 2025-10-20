package nus.edu.u.domain.dto.test;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushMessageDTO {
    private String recipientToken;
    private String title;
    private String body;
    private String image;
    private Map<String, String> data;
}
