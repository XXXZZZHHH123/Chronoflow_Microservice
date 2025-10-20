package nus.edu.u.shared.rpc.notification.service;

import nus.edu.u.shared.rpc.notification.dto.member.RegSearchReqDTO;

public interface MemberNotificationService {
    String sendMemberInviteEmail(RegSearchReqDTO req);
}
