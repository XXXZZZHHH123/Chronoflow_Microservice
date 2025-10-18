package nus.edu.u.wsgateway.dto;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class WsPushRequestDTO {
    String userId;           // target user
    String eventId;          // idempotent key (opaque here)
    String type;             // e.g. "new-task-assigned"
    String title;            // optional
    String body;             // optional
    Map<String, Object> data;// arbitrary payload (deepLink, taskId, etc.)
}
