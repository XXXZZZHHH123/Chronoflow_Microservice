package nus.edu.u.user.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import nus.edu.u.shared.rpc.notification.dto.common.NotificationRequestDTO;
import nus.edu.u.shared.rpc.notification.enums.NotificationChannel;
import nus.edu.u.shared.rpc.notification.enums.NotificationEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherTest {

    @Mock private PubSubTemplate pubSubTemplate;

    private ObjectMapper objectMapper;
    private NotificationPublisher publisher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        publisher = new NotificationPublisher(pubSubTemplate, objectMapper);
    }

    @Test
    void publish_validRequest_sendsToPubSubWithAttributes() throws Exception {
        NotificationRequestDTO req =
                NotificationRequestDTO.builder()
                        .eventId("evt-1")
                        .channel(NotificationChannel.EMAIL)
                        .type(NotificationEventType.MEMBER_INVITE)
                        .to("user@example.com")
                        .userId("user-1")
                        .build();
        CompletableFuture<String> future = CompletableFuture.completedFuture("msg-123");
        when(pubSubTemplate.publish(anyString(), anyString(), anyMap())).thenReturn(future);

        String messageId = publisher.publish(req);

        assertThat(messageId).isNotNull();
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, String>> attrsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pubSubTemplate)
                .publish(
                        eq("chronoflow-notification"),
                        payloadCaptor.capture(),
                        attrsCaptor.capture());

        assertThat(payloadCaptor.getValue()).contains("\"eventId\":\"evt-1\"");
        assertThat(attrsCaptor.getValue())
                .containsEntry("eventId", "evt-1")
                .containsEntry("channel", "EMAIL")
                .containsEntry("type", "MEMBER_INVITE")
                .containsEntry("userId", "user-1")
                .containsEntry("to", "user@example.com");
    }

    @Test
    void publish_whenEventIdMissing_throwsIllegalArgumentException() {
        NotificationRequestDTO req =
                NotificationRequestDTO.builder()
                        .channel(NotificationChannel.EMAIL)
                        .eventId("")
                        .build();

        assertThatThrownBy(() -> publisher.publish(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eventId");
    }

    @Test
    void publish_whenChannelMissing_throwsIllegalArgumentException() {
        NotificationRequestDTO req = NotificationRequestDTO.builder().eventId("evt-2").build();

        assertThatThrownBy(() -> publisher.publish(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("channel");
    }

    @Test
    void publish_whenPubSubFails_wrapsException() {
        NotificationRequestDTO req =
                NotificationRequestDTO.builder()
                        .eventId("evt-3")
                        .channel(NotificationChannel.EMAIL)
                        .build();
        when(pubSubTemplate.publish(anyString(), anyString(), anyMap()))
                .thenThrow(new RuntimeException("down"));

        assertThatThrownBy(() -> publisher.publish(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pub/Sub publish failed")
                .hasRootCauseInstanceOf(RuntimeException.class);
    }
}
