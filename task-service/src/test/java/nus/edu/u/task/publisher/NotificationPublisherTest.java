package nus.edu.u.task.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherTest {

    private static final String TOPIC = "chronoflow-notification";

    @Mock private PubSubTemplate pubSubTemplate;

    @Mock private ObjectMapper objectMapper;

    @InjectMocks private NotificationPublisher notificationPublisher;

    @Test
    void publishSuccessPublishesJsonWithAttributes() throws Exception {
        NotificationRequestDTO request =
                NotificationRequestDTO.builder()
                        .eventId("event-123")
                        .channel(NotificationChannel.EMAIL)
                        .type(NotificationEventType.NEW_TASK_ASSIGN)
                        .userId("user-7")
                        .to("user@example.com")
                        .build();

        String payload = "{\"eventId\":\"event-123\"}";
        when(objectMapper.writeValueAsString(request)).thenReturn(payload);
        StubFuture future = new StubFuture("message-42");
        Mockito.doReturn(future)
                .when(pubSubTemplate)
                .publish(eq(TOPIC), eq(payload), Mockito.<Map<String, String>>any());

        String messageId = notificationPublisher.publish(request);

        assertThat(messageId).isEqualTo("message-42");

        ArgumentCaptor<Map<String, String>> attrCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pubSubTemplate).publish(eq(TOPIC), eq(payload), attrCaptor.capture());
        Map<String, String> attributes = attrCaptor.getValue();

        assertThat(attributes)
                .containsEntry("eventId", "event-123")
                .containsEntry("channel", NotificationChannel.EMAIL.name())
                .containsEntry("type", NotificationEventType.NEW_TASK_ASSIGN.name())
                .containsEntry("userId", "user-7")
                .containsEntry("to", "user@example.com");

        verify(objectMapper).writeValueAsString(request);
    }

    @Test
    void publishSkipsBlankOptionalAttributes() throws Exception {
        NotificationRequestDTO request =
                NotificationRequestDTO.builder()
                        .eventId("event-xyz")
                        .channel(NotificationChannel.WS)
                        .type(NotificationEventType.NEW_TASK_ASSIGN)
                        .userId("user-9")
                        .to("  ") // should be ignored
                        .build();

        when(objectMapper.writeValueAsString(request)).thenReturn("{}");
        StubFuture future = new StubFuture("ignored");
        Mockito.doReturn(future)
                .when(pubSubTemplate)
                .publish(eq(TOPIC), eq("{}"), Mockito.<Map<String, String>>any());

        notificationPublisher.publish(request);

        ArgumentCaptor<Map<String, String>> attrCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pubSubTemplate).publish(eq(TOPIC), eq("{}"), attrCaptor.capture());
        Map<String, String> attributes = attrCaptor.getValue();

        assertThat(attributes).containsKeys("eventId", "channel", "type", "userId");
        assertThat(attributes).doesNotContainKey("to");
    }

    @Test
    void publishThrowsWhenEventIdMissing() {
        NotificationRequestDTO request =
                NotificationRequestDTO.builder()
                        .channel(NotificationChannel.EMAIL)
                        .type(NotificationEventType.NEW_TASK_ASSIGN)
                        .build();

        assertThatThrownBy(() -> notificationPublisher.publish(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eventId is required");
    }

    @Test
    void publishThrowsWhenChannelMissing() {
        NotificationRequestDTO request =
                NotificationRequestDTO.builder()
                        .eventId("event-1")
                        .type(NotificationEventType.NEW_TASK_ASSIGN)
                        .build();

        assertThatThrownBy(() -> notificationPublisher.publish(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("channel is required");
    }

    @Test
    void publishWrapsSerializationFailures() throws Exception {
        NotificationRequestDTO request =
                NotificationRequestDTO.builder()
                        .eventId("event-1")
                        .channel(NotificationChannel.EMAIL)
                        .type(NotificationEventType.NEW_TASK_ASSIGN)
                        .build();

        RuntimeException root = new RuntimeException("boom");
        when(objectMapper.writeValueAsString(request)).thenThrow(root);

        assertThatThrownBy(() -> notificationPublisher.publish(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pub/Sub publish failed")
                .hasCause(root);
    }

    @Test
    void publishWrapsPubSubFailures() throws Exception {
        NotificationRequestDTO request =
                NotificationRequestDTO.builder()
                        .eventId("event-1")
                        .channel(NotificationChannel.EMAIL)
                        .type(NotificationEventType.NEW_TASK_ASSIGN)
                        .build();

        when(objectMapper.writeValueAsString(request)).thenReturn("{\"eventId\":\"event-1\"}");
        RuntimeException root = new RuntimeException("pubsub down");
        Mockito.doThrow(root)
                .when(pubSubTemplate)
                .publish(
                        eq(TOPIC),
                        eq("{\"eventId\":\"event-1\"}"),
                        Mockito.<Map<String, String>>any());

        assertThatThrownBy(() -> notificationPublisher.publish(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pub/Sub publish failed")
                .hasCause(root);
    }

    /**
     * Minimal ListenableFuture stub whose toString returns a deterministic message id so that the
     * NotificationPublisher behaviour can be asserted.
     */
    private static final class StubFuture extends CompletableFuture<String> {
        private final String messageId;

        private StubFuture(String messageId) {
            this.messageId = messageId;
            this.complete(messageId);
        }

        @Override
        public String toString() {
            return messageId;
        }
    }
}
