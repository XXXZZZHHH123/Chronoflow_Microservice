package nus.edu.u.user.publisher.organizer;

import lombok.RequiredArgsConstructor;
import nus.edu.u.shared.rpc.notification.dto.organizer.RegOrganizerReqDTO;
import nus.edu.u.shared.rpc.notification.service.OrganizerNotificationService;
import nus.edu.u.user.mapper.notification.OrganizerNotificationMapper;
import nus.edu.u.user.publisher.NotificationPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizerNotificationPublisher implements OrganizerNotificationService {

    private final NotificationPublisher notificationPublisher;

    @Override
    public String sendWelcomeOrganizerEmail(RegOrganizerReqDTO req) {
        return notificationPublisher.publish(
                OrganizerNotificationMapper.RegOrganizerToNotification(req));
    }
}
