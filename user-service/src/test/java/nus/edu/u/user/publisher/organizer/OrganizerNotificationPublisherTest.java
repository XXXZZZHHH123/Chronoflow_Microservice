package nus.edu.u.user.publisher.organizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nus.edu.u.shared.rpc.notification.dto.common.NotificationRequestDTO;
import nus.edu.u.shared.rpc.notification.dto.organizer.RegOrganizerReqDTO;
import nus.edu.u.shared.rpc.notification.enums.NotificationChannel;
import nus.edu.u.shared.rpc.notification.enums.NotificationEventType;
import nus.edu.u.user.publisher.NotificationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizerNotificationPublisherTest {

    @Mock private NotificationPublisher notificationPublisher;

    private OrganizerNotificationPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new OrganizerNotificationPublisher(notificationPublisher);
    }

    @Test
    void sendWelcomeOrganizerEmail_convertsAndDelegatesToNotificationPublisher() {
        RegOrganizerReqDTO req =
                RegOrganizerReqDTO.builder()
                        .name("Organizer Name")
                        .username("org-user")
                        .userEmail("org@example.com")
                        .mobile("12345")
                        .organizationName("Org Ltd")
                        .organizationAddress("Street 1")
                        .organizationCode("ORG001")
                        .build();
        when(notificationPublisher.publish(any(NotificationRequestDTO.class)))
                .thenReturn("msg-xyz");

        String messageId = publisher.sendWelcomeOrganizerEmail(req);

        assertThat(messageId).isEqualTo("msg-xyz");
        ArgumentCaptor<NotificationRequestDTO> captor =
                ArgumentCaptor.forClass(NotificationRequestDTO.class);
        verify(notificationPublisher).publish(captor.capture());

        NotificationRequestDTO dto = captor.getValue();
        assertThat(dto.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(dto.getType()).isEqualTo(NotificationEventType.ORGANIZER_WELCOME);
        assertThat(dto.getTo()).isEqualTo("org@example.com");
        assertThat(dto.getUserId()).isEqualTo("org-user");
        assertThat(dto.getEventId()).isEqualTo("organizer-registration-ORG001");
        assertThat(dto.getVariables())
                .containsEntry("organizationName", "Org Ltd")
                .containsEntry("userEmail", "org@example.com");
    }
}
