package nus.edu.u.attendee.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock private PubSubTemplate pubSubTemplate;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private NotificationPublisher publisher;

    @Test
    void publishSuccessPublishesJsonWithAttributes() throws Exception {
        NotificationRequestDTO request =
                NotificationRequestDTO.builder()
                        .eventId("evt-1")
                        .channel(NotificationChannel.EMAIL)
                        .type(NotificationEventType.ORGANIZER_WELCOME)
                        .userId("user-7")
                        .to("user@example.com")
                        .build();

        String payload = "{\"eventId\":\"evt-1\"}";
        when(objectMapper.writeValueAsString(request)).thenReturn(payload);
        StubFuture future = new StubFuture("msg-123");
        Mockito.doReturn(future)
                .when(pubSubTemplate)
                .publish(eq("chronoflow-notification"), eq(payload), any());

        String messageId = publisher.publish(request);

        assertThat(messageId).isEqualTo("msg-123");

        ArgumentCaptor<Map<String, String>> attrCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pubSubTemplate)
                .publish(eq("chronoflow-notification"), eq(payload), attrCaptor.capture());
        Map<String, String> attributes = attrCaptor.getValue();
        assertThat(attributes)
                .containsEntry("eventId", "evt-1")
                .containsEntry("channel", "EMAIL")
                .containsEntry("type", "ORGANIZER_WELCOME")
                .containsEntry("userId", "user-7")
                .containsEntry("to", "user@example.com");
    }

    @Test
    void publishSkipsBlankAttributes() throws Exception {
        NotificationRequestDTO request =
                NotificationRequestDTO.builder()
                        .eventId("evt-2")
                        .channel(NotificationChannel.PUSH)
                        .type(NotificationEventType.NEW_TASK_ASSIGN)
                        .userId("user-5")
                        .to("   ")
                        .build();

        when(objectMapper.writeValueAsString(request)).thenReturn("{}");
        StubFuture future = new StubFuture("ignored");
        Mockito.doReturn(future)
                .when(pubSubTemplate)
                .publish(eq("chronoflow-notification"), eq("{}"), any());

        publisher.publish(request);

        ArgumentCaptor<Map<String, String>> attrCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pubSubTemplate)
                .publish(eq("chronoflow-notification"), eq("{}"), attrCaptor.capture());
        Map<String, String> attributes = attrCaptor.getValue();
        assertThat(attributes).containsKeys("eventId", "channel", "type", "userId");
        assertThat(attributes).doesNotContainKey("to");
    }

    @Test
    void publishThrowsWhenEventIdMissing() {
        NotificationRequestDTO request =
                NotificationRequestDTO.builder()
                        .channel(NotificationChannel.EMAIL)
                        .type(NotificationEventType.ORGANIZER_WELCOME)
                        .build();

        assertThatThrownBy(() -> publisher.publish(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eventId");
    }

    @Test
    void publishThrowsWhenChannelMissing() {
        NotificationRequestDTO request =
                NotificationRequestDTO.builder()
                        .eventId("evt-3")
                        .type(NotificationEventType.ORGANIZER_WELCOME)
                        .build();

        assertThatThrownBy(() -> publisher.publish(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("channel");
    }

    @Test
    void publishWrapsSerializationException() throws Exception {
        NotificationRequestDTO request =
                NotificationRequestDTO.builder()
                        .eventId("evt-4")
                        .channel(NotificationChannel.EMAIL)
                        .type(NotificationEventType.ORGANIZER_WELCOME)
                        .build();
        RuntimeException root = new RuntimeException("boom");
        when(objectMapper.writeValueAsString(request)).thenThrow(root);

        assertThatThrownBy(() -> publisher.publish(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pub/Sub publish failed")
                .hasCause(root);
    }

    @Test
    void publishWrapsPubSubException() throws Exception {
        NotificationRequestDTO request =
                NotificationRequestDTO.builder()
                        .eventId("evt-5")
                        .channel(NotificationChannel.EMAIL)
                        .type(NotificationEventType.ORGANIZER_WELCOME)
                        .build();

        when(objectMapper.writeValueAsString(request)).thenReturn("{\"eventId\":\"evt-5\"}");
        RuntimeException root = new RuntimeException("pubsub down");
        Mockito.doThrow(root)
                .when(pubSubTemplate)
                .publish(eq("chronoflow-notification"), eq("{\"eventId\":\"evt-5\"}"), any());

        assertThatThrownBy(() -> publisher.publish(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pub/Sub publish failed")
                .hasCause(root);
    }

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
