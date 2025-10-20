package nus.edu.u.wsgateway.domain;

import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("ws_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(name = "ux_user_event", def = "{'userId':1,'eventId':1}", unique = true),
    @CompoundIndex(name = "idx_user_created_desc", def = "{'userId':1,'createdAt':-1}"),
    @CompoundIndex(name = "idx_user_seen", def = "{'userId':1,'seenAt':1}")
})
public class NotificationFeedDoc {
    @Id private String id;

    private String userId;
    private String eventId;
    private String type;
    private String title;
    private String body;
    private Map<String, Object> data;

    @CreatedDate private Instant createdAt;
    private Instant deliveredAt;
    private Instant seenAt;
    private Instant openedAt;
    @LastModifiedDate private Instant updatedAt;
}
