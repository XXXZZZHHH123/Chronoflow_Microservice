package nus.edu.u.wsgateway.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkSeenRequestDTO {
    String userId;
    List<String> notificationIds;
}
