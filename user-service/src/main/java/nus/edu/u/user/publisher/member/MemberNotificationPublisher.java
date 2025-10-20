package nus.edu.u.user.publisher.member;

import lombok.RequiredArgsConstructor;
import nus.edu.u.shared.rpc.notification.dto.member.RegSearchReqDTO;
import nus.edu.u.shared.rpc.notification.service.MemberNotificationService;
import nus.edu.u.user.mapper.notification.MemberNotificationMapper;
import nus.edu.u.user.publisher.NotificationPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberNotificationPublisher implements MemberNotificationService {

    private final NotificationPublisher notificationPublisher;

    @Override
    public String sendMemberInviteEmail(RegSearchReqDTO req) {
        return notificationPublisher.publish(MemberNotificationMapper.RegMemberToNotification(req));
    }
}
