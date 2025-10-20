package nus.edu.u.services.domains.attendee;

import nus.edu.u.domain.dto.email.AttendeeInviteReqDTO;

public interface AttendeeNotificationService {
    String sendAttendeeInviteEmail(AttendeeInviteReqDTO req);
}
