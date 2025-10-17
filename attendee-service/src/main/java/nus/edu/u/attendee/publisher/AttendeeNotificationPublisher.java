package nus.edu.u.attendee.publisher;
import nus.edu.u.attendee.mapper.notification.AttendeeNotificationMapper;
import nus.edu.u.shared.rpc.notification.dto.Attendee.AttendeeInviteReqDTO;
import nus.edu.u.shared.rpc.notification.service.AttendeeNotificationService;
import org.springframework.stereotype.Component;

@Component
public class AttendeeNotificationPublisher implements AttendeeNotificationService {

    private NotificationPublisher notificationPublisher;
    private AttendeeNotificationMapper mapper;

    public String sendAttendeeInviteEmail(AttendeeInviteReqDTO reqDTO)
    {
        notificationPublisher.publish(AttendeeNotificationMapper.attendeeInvitationToNotification(reqDTO));

        return "Ok";
    }
}
