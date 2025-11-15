package nus.edu.u.provider.ws;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.configuration.ws.WsGatewayLimitPropertiesConfig;
import nus.edu.u.domain.dto.ws.WsRequestDTO;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

@Slf4j
public class FluxWSClient implements WSClient {

    private final WsGatewayLimitPropertiesConfig props;
    private final WebClient client;

    private FluxWSClient(WsGatewayLimitPropertiesConfig props, WebClient client) {
        this.props = props;
        this.client = client;
    }

    public static FluxWSClient defaultClient() {
        WsGatewayLimitPropertiesConfig props = WsGatewayLimitPropertiesConfig.CURRENT;
        if (props == null) {
            throw new IllegalStateException(
                    "WsGatewayLimitPropertiesConfig not initialized. " +
                            "Make sure Spring has started and loaded notification.ws.* properties."
            );
        }

        HttpClient http = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getTimeouts().getConnectMs())
                .responseTimeout(Duration.ofMillis(props.getTimeouts().getReadMs()))
                .doOnConnected(c ->
                        c.addHandlerLast(new WriteTimeoutHandler(
                                props.getTimeouts().getWriteMs(), TimeUnit.MILLISECONDS)));

        WebClient webClient = WebClient.builder()
                .baseUrl(props.getBaseUrl()) // <-- baseUrl from config
                .clientConnector(new ReactorClientHttpConnector(http))
                .defaultHeader("X-Source-Service",
                        System.getProperty("spring.application.name", "notificationservice"))
                .filter(errorFilter())
                .build();

        return new FluxWSClient(props, webClient);
    }

    @Override
    public Mono<Void> sendWSNotification(WsRequestDTO req) {
        if (!props.isEnabled()) return Mono.empty();
        String reqId = MDC.get("requestId");
        return client.post()
                .uri("/ws/internal/push")
                .header("X-Request-Id", reqId == null ? "" : reqId)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(Retry.backoff(
                                props.getRetry().getMaxRetries(),
                                Duration.ofMillis(props.getRetry().getInitialBackoffMs()))
                        .jitter(0.2)
                        .filter(FluxWSClient::isRetryable));
    }

    private static boolean isRetryable(Throwable t) {
        if (t instanceof java.net.ConnectException) return true;
        if (t instanceof WebClientResponseException w) {
            int s = w.getStatusCode().value();
            return s >= 500 && s < 600;
        }
        return false;
    }

    private static ExchangeFilterFunction errorFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(resp -> {
            if (resp.statusCode().is2xxSuccessful()) return Mono.just(resp);
            return resp.bodyToMono(String.class).defaultIfEmpty("")
                    .flatMap(body -> Mono.error(WebClientResponseException.create(
                            resp.statusCode().value(),
                            (resp.statusCode() instanceof HttpStatus hs) ? hs.getReasonPhrase() : "",
                            resp.headers().asHttpHeaders(),
                            body.getBytes(StandardCharsets.UTF_8),
                            StandardCharsets.UTF_8)));
        });
    }
}