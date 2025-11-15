package nus.edu.u.services.clients;

import nus.edu.u.domain.dto.clients.RegSearchReqDTO;

public interface MemberEmailService {
    void sendMemberInviteEmail(String recipientEmail, RegSearchReqDTO req);
}
