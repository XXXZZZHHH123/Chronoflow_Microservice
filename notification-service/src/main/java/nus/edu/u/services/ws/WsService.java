package nus.edu.u.services.ws;

import nus.edu.u.domain.dto.ws.WsRequestDTO;

public interface WsService {

    /**
     * Convenience method used by senders: builds the proper recipientKey for a user and delegates
     * to send().
     */
    String sendToUser(String userId, WsRequestDTO base);

    /**
     * Core single-recipient send. Creates the parent delivery row, rate-limits, invokes the
     * gateway.
     */
    String send(WsRequestDTO dto);
}
