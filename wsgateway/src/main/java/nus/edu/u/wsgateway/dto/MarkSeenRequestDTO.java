package nus.edu.u.wsgateway.dto;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkSeenRequestDTO {
    String userId;
    List<String> notificationIds;
}
