package nus.edu.u.services.clients;

import nus.edu.u.domain.dto.clients.AttendeeInviteReqDTO;

public interface AttendeeEmailService {
    void sendAttendeeInvite(AttendeeInviteReqDTO req);
}
