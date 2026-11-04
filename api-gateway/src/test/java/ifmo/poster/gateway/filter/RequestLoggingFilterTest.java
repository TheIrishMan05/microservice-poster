package ifmo.poster.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void addsRequestIdWhenItIsMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/events/1").build());
        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();
        GatewayFilterChain chain = current -> {
            forwardedExchange.set(current);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        String requestId = forwardedExchange.get().getRequest().getHeaders()
                .getFirst(RequestLoggingFilter.REQUEST_ID_HEADER);
        assertThat(requestId).isNotBlank();
        assertThat(exchange.getResponse().getHeaders()
                .getFirst(RequestLoggingFilter.REQUEST_ID_HEADER)).isEqualTo(requestId);
    }

    @Test
    void keepsRequestIdProvidedByCaller() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders/1")
                        .header(RequestLoggingFilter.REQUEST_ID_HEADER, "known-id")
                        .build());
        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();

        StepVerifier.create(filter.filter(exchange, current -> {
                    forwardedExchange.set(current);
                    return Mono.empty();
                }))
                .verifyComplete();

        assertThat(forwardedExchange.get().getRequest().getHeaders()
                .getFirst(RequestLoggingFilter.REQUEST_ID_HEADER)).isEqualTo("known-id");
    }
}
