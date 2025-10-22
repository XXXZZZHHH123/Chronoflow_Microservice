package nus.edu.u.user.publisher.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nus.edu.u.shared.rpc.notification.dto.common.NotificationRequestDTO;
import nus.edu.u.shared.rpc.notification.dto.member.RegSearchReqDTO;
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
class MemberNotificationPublisherTest {

    @Mock private NotificationPublisher notificationPublisher;

    private MemberNotificationPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new MemberNotificationPublisher(notificationPublisher);
    }

    @Test
    void sendMemberInviteEmail_convertsAndDelegatesToNotificationPublisher() {
        RegSearchReqDTO req =
                RegSearchReqDTO.builder()
                        .organizationId(99L)
                        .userId(123L)
                        .recipientEmail("member@example.com")
                        .build();
        when(notificationPublisher.publish(any(NotificationRequestDTO.class))).thenReturn("msg-9");

        String messageId = publisher.sendMemberInviteEmail(req);

        assertThat(messageId).isEqualTo("msg-9");
        ArgumentCaptor<NotificationRequestDTO> captor =
                ArgumentCaptor.forClass(NotificationRequestDTO.class);
        verify(notificationPublisher).publish(captor.capture());

        NotificationRequestDTO dto = captor.getValue();
        assertThat(dto.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(dto.getType()).isEqualTo(NotificationEventType.MEMBER_INVITE);
        assertThat(dto.getTo()).isEqualTo("member@example.com");
        assertThat(dto.getEventId()).isEqualTo("member-invitation-99-123");
        assertThat(dto.getVariables())
                .containsEntry("organizationId", 99L)
                .containsEntry("userId", 123L)
                .containsEntry("recipientEmail", "member@example.com");
    }
}
