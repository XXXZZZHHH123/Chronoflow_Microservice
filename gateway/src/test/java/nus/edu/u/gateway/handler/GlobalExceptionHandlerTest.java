package nus.edu.u.gateway.handler;

import static org.assertj.core.api.Assertions.assertThat;

import nus.edu.u.common.core.domain.CommonResult;
import nus.edu.u.common.utils.json.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handle_withGenericException_returnsInternalError() {
        MockServerWebExchange exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/error").build());

        StepVerifier.create(handler.handle(exchange, new RuntimeException("boom")))
                .verifyComplete();

        CommonResult<?> result = parseResponse(exchange);
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMsg()).isEqualTo("System error");
    }

    @Test
    void handle_withResponseStatusException_usesStatusCode() {
        MockServerWebExchange exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/missing").build());
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found");

        StepVerifier.create(handler.handle(exchange, ex)).verifyComplete();

        CommonResult<?> result = parseResponse(exchange);
        assertThat(result.getCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getMsg()).isEqualTo("Not Found");
    }

    @Test
    void handle_whenResponseCommitted_propagatesError() {
        MockServerWebExchange exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/committed").build());
        exchange.getResponse().setComplete().block();

        StepVerifier.create(handler.handle(exchange, new RuntimeException("boom")))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException)
                .verify();
    }

    private CommonResult<?> parseResponse(MockServerWebExchange exchange) {
        MockServerHttpResponse response = (MockServerHttpResponse) exchange.getResponse();
        String body = response.getBodyAsString().block();
        return JsonUtils.parseObject(body, CommonResult.class);
    }
}
