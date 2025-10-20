package nus.edu.u.domain.dto.ws;

import java.util.Map;
import lombok.Builder;
import lombok.Value;
import nus.edu.u.enums.common.NotificationEventType;

/**
 * Channel-specific DTO for WebSocket delivery. Mirrors PushRequestDTO shape but without
 * token/device details.
 */
@Value
@Builder
public class WsRequestDTO {

    /** Idempotency key (event scope) */
    String eventId;

    /** For idempotency at the parent table (e.g. "ws:user:<userId>") */
    String recipientKey;

    /** Convenience field so service can build recipientKey; not persisted */
    String userId;

    /** Rendered text */
    String title;

    String body;

    /** Extra payload (deepLink, screen, ids, etc.) */
    Map<String, Object> data;

    /** Domain event type */
    NotificationEventType type;
}
