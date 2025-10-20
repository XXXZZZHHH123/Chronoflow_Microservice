package nus.edu.u.shared.rpc.notification.service;

import nus.edu.u.shared.rpc.notification.dto.Attendee.AttendeeInviteReqDTO;

public interface AttendeeNotificationService {
    String sendAttendeeInviteEmail(AttendeeInviteReqDTO reqDTO);
}
