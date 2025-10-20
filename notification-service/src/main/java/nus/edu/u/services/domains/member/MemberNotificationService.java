package nus.edu.u.services.domains.member;

import nus.edu.u.domain.dto.email.RegSearchReqDTO;

public interface MemberNotificationService {
    String sendMemberInviteEmail(RegSearchReqDTO req);
}
