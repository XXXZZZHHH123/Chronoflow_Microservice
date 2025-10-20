package nus.edu.u.services.domains.organizer;

import nus.edu.u.domain.dto.email.RegOrganizerReqDTO;

public interface OrganizerNotificationService {
    /**
     * Sends the “Organizer Welcome” email and returns the request id (or "ALREADY_ACCEPTED" if
     * idempotency hits).
     */
    String sendWelcomeOrganizerEmail(RegOrganizerReqDTO req);
}
