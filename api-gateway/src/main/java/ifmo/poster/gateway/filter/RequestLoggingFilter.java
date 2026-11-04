package ifmo.poster.gateway.filter;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = Optional.ofNullable(
                        exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER))
                .filter(value -> !value.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.set(REQUEST_ID_HEADER, requestId))
                .build();
        ServerWebExchange updatedExchange = exchange.mutate().request(request).build();
        updatedExchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

        long startedAt = System.nanoTime();
        log.info("Gateway request: id={}, method={}, path={}",
                requestId, request.getMethod(), request.getURI().getPath());

        return chain.filter(updatedExchange)
                .doFinally(signal -> log.info(
                        "Gateway response: id={}, status={}, durationMs={}",
                        requestId,
                        updatedExchange.getResponse().getStatusCode(),
                        (System.nanoTime() - startedAt) / 1_000_000
                ));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
