package nus.edu.u.wsgateway.dto;

import java.util.List;
import lombok.Value;

@Value
public class MarkSeenRequestDTO {
    String userId;
    List<String> notificationIds;
}
