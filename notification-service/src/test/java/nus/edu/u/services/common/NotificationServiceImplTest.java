package nus.edu.u.services.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nus.edu.u.core.common.NotificationSender;
import nus.edu.u.core.common.NotificationSenderFactory;
import nus.edu.u.domain.dto.common.NotificationRequestDTO;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationSenderFactory senderFactory;
    @Mock private NotificationSender sender;

    @InjectMocks private NotificationServiceImpl service;

    private NotificationRequestDTO request;

    @BeforeEach
    void setUp() {
        request =
                NotificationRequestDTO.builder()
                        .channel(NotificationChannel.EMAIL)
                        .eventId("evt-1")
                        .type(NotificationEventType.MEMBER_INVITE)
                        .build();
    }

    @Test
    void sendDelegatesToResolvedSender() {
        when(senderFactory.strategy(NotificationChannel.EMAIL)).thenReturn(sender);
        when(sender.send(request)).thenReturn("message-id");

        String result = service.send(request);

        assertThat(result).isEqualTo("message-id");
        verify(senderFactory).strategy(NotificationChannel.EMAIL);
        verify(sender).send(request);
    }
}
