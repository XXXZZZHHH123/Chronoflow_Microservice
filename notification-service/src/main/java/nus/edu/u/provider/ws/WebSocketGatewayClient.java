package nus.edu.u.provider.ws;

import nus.edu.u.domain.dto.ws.WsRequestDTO;
import reactor.core.publisher.Mono;

public interface WebSocketGatewayClient {
    Mono<Void> sendToUser(WsRequestDTO req);

    default void sendToUserFireAndForget(WsRequestDTO req) {
        sendToUser(req).subscribe(null, e -> {});
    }
}
