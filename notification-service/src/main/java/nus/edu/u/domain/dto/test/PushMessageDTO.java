package nus.edu.u.domain.dto.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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
