package nus.edu.u.wsgateway.dto;

import lombok.Value;

import java.util.List;

@Value
public class MarkSeenRequestDTO {
    String userId;
    List<String> notificationIds;
}
