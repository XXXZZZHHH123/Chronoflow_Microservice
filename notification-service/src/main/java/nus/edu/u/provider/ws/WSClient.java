package nus.edu.u.provider.ws;

import nus.edu.u.domain.dto.ws.WsRequestDTO;
import reactor.core.publisher.Mono;

public interface WSClient {

    Mono<Void> sendWSNotification(WsRequestDTO req);
}
