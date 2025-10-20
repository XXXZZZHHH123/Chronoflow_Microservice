package nus.edu.u.attendee.publisher;

import lombok.RequiredArgsConstructor;
import nus.edu.u.attendee.mapper.notification.AttendeeNotificationMapper;
import nus.edu.u.shared.rpc.notification.dto.Attendee.AttendeeInviteReqDTO;
import nus.edu.u.shared.rpc.notification.service.AttendeeNotificationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendeeNotificationPublisher implements AttendeeNotificationService {

    private final NotificationPublisher notificationPublisher;

    @Override
    public String sendAttendeeInviteEmail(AttendeeInviteReqDTO reqDTO) {
        return notificationPublisher.publish(
                AttendeeNotificationMapper.attendeeInvitationToNotification(reqDTO));
    }
}
