package nus.edu.u.domain.dto.test;

import lombok.Data;

@Data
public class NotificationEventDTO {
    private String id;
    private String type;
    private String title;
    private String message;
    private String userId;
    private String priority;
}
