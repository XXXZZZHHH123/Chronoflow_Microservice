package nus.edu.u.shared.rpc.notification.service;

import nus.edu.u.shared.rpc.notification.dto.organizer.RegOrganizerReqDTO;

public interface OrganizerNotificationService {
    /**
     * Sends the “Organizer Welcome” email and returns the request id (or "ALREADY_ACCEPTED" if
     * idempotency hits).
     */
    String sendWelcomeOrganizerEmail(RegOrganizerReqDTO req);
}
