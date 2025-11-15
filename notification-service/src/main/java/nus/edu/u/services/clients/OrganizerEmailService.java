package nus.edu.u.services.clients;


import nus.edu.u.domain.dto.clients.RegOrganizerReqDTO;

public interface OrganizerEmailService {
    /**
     * Sends the “Organizer Welcome” email and returns the request id (or "ALREADY_ACCEPTED" if
     * idempotency hits).
     */
    void sendWelcomeEmailOrganizer(RegOrganizerReqDTO req);
}
