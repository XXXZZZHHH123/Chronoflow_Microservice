package nus.edu.u.gateway.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

class WebFrameworkUtilsTest {

    @Test
    void setTenantIdHeader_setsWhenTenantPresent() {
        HttpHeaders headers = new HttpHeaders();

        WebFrameworkUtils.setTenantIdHeader(42L, headers);

        assertThat(headers.getFirst("tenant-id")).isEqualTo("42");
    }

    @Test
    void setTenantIdHeader_skipsWhenTenantMissing() {
        HttpHeaders headers = new HttpHeaders();

        WebFrameworkUtils.setTenantIdHeader(null, headers);

        assertThat(headers.containsKey("tenant-id")).isFalse();
    }

    @Test
    void getTenantId_returnsNumericTenant() {
        MockServerWebExchange exchange =
                MockServerWebExchange.from(
                        MockServerHttpRequest.get("/test").header("tenant-id", "123").build());

        assertThat(WebFrameworkUtils.getTenantId(exchange)).isEqualTo(123L);
    }

    @Test
    void getTenantId_returnsNullWhenNotNumeric() {
        MockServerWebExchange exchange =
                MockServerWebExchange.from(
                        MockServerHttpRequest.get("/test").header("tenant-id", "abc").build());

        assertThat(WebFrameworkUtils.getTenantId(exchange)).isNull();
    }

    @Test
    void writeJSON_writesJsonBodyAndContentType() {
        MockServerWebExchange exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

        StepVerifier.create(WebFrameworkUtils.writeJSON(exchange, Map.of("status", "ok")))
                .verifyComplete();

        MockServerHttpResponse response = (MockServerHttpResponse) exchange.getResponse();
        assertThat(response.getHeaders().getContentType())
                .isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        StepVerifier.create(response.getBodyAsString())
                .expectNext("{\"status\":\"ok\"}")
                .verifyComplete();
    }

    @Test
    void getClientIP_returnsFromForwardedHeader() {
        MockServerWebExchange exchange =
                MockServerWebExchange.from(
                        MockServerHttpRequest.get("/test")
                                .header("X-Forwarded-For", "203.0.113.1")
                                .build());

        assertThat(WebFrameworkUtils.getClientIP(exchange)).isEqualTo("203.0.113.1");
    }

    @Test
    void getClientIP_fallsBackToRemoteAddress() {
        MockServerWebExchange exchange =
                MockServerWebExchange.from(
                        MockServerHttpRequest.get("/test")
                                .remoteAddress(
                                        InetSocketAddress.createUnresolved("198.51.100.5", 8080))
                                .build());

        assertThat(WebFrameworkUtils.getClientIP(exchange)).isEqualTo("198.51.100.5");
    }

    @Test
    void getGatewayRoute_returnsRouteFromAttributes() {
        MockServerWebExchange exchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        Route route =
                Route.async()
                        .id("route-1")
                        .uri("http://localhost")
                        .predicate(serverWebExchange -> true)
                        .build();
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);

        assertThat(WebFrameworkUtils.getGatewayRoute(exchange)).isSameAs(route);
    }
}
