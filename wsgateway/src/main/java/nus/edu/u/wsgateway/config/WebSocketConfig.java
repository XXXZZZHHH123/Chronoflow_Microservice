package nus.edu.u.wsgateway.config;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

@Configuration
@EnableWebFlux
@RequiredArgsConstructor
public class WebSocketConfig {

    private final WebSocketHandler wsHandler;

    /** Tune the WS upgrade (e.g., max frame size) */
    @Bean
    public WebSocketService webSocketService() {
        ReactorNettyRequestUpgradeStrategy strategy = new ReactorNettyRequestUpgradeStrategy();
        strategy.setMaxFramePayloadLength(64 * 1024); // 64KB frames; adjust as needed
        return new HandshakeWebSocketService(strategy);
    }

    /** Adapter uses the above WebSocketService */
    @Bean
    public WebSocketHandlerAdapter handlerAdapter(WebSocketService webSocketService) {
        return new WebSocketHandlerAdapter(webSocketService);
    }

    /** Map the WebSocket endpoint and attach per-path CORS for WS handshakes */
    @Bean
    public SimpleUrlHandlerMapping webSocketMapping() {
        var mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(
                Map.of(
                        "/ws", wsHandler // ws://<host>/ws?userId=...
                        ));
        mapping.setOrder(-1); // before standard HTTP mappings

        // CORS for WebSocket handshake (origin check)
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(
                List.of(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:5174",
                        "http://localhost:8080"));
        cors.setAllowCredentials(true);
        cors.setAllowedHeaders(List.of("*"));
        cors.setAllowedMethods(List.of("GET")); // WS handshake is GET

        mapping.setCorsConfigurations(Map.of("/ws", cors));
        return mapping;
    }
}
