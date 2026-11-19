package ifmo.poster.gateway.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GatewayErrorWebExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GatewayErrorWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = resolveStatus(ex);
        String title = switch (status) {
            case NOT_FOUND -> "Маршрут или сервис не найден";
            case SERVICE_UNAVAILABLE -> "Сервис временно недоступен";
            case BAD_GATEWAY -> "Ошибка шлюза";
            case GATEWAY_TIMEOUT -> "Таймаут шлюза";
            default -> "Ошибка шлюза";
        };
        String detail = resolveDetail(ex, status, exchange.getRequest().getURI().getPath());

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", title);
        body.put("status", status.value());
        body.put("detail", detail);
        body.put("instance", exchange.getRequest().getPath().value());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException encodingError) {
            bytes = ("{\"title\":\"" + title + "\",\"status\":" + status.value()
                    + ",\"detail\":\"" + detail + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private static HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException responseStatusException) {
            HttpStatus resolved = HttpStatus.resolve(responseStatusException.getStatusCode().value());
            return resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = String.valueOf(ex.getMessage()).toLowerCase();
        if (message.contains("connection refused")
                || message.contains("no servers available")
                || message.contains("service unavailable")
                || message.contains("unable to find instance")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        if (message.contains("timeout")) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        return HttpStatus.BAD_GATEWAY;
    }

    private static String resolveDetail(Throwable ex, HttpStatus status, String path) {
        if (ex instanceof ResponseStatusException responseStatusException
                && responseStatusException.getReason() != null
                && !responseStatusException.getReason().isBlank()) {
            return responseStatusException.getReason();
        }
        return switch (status) {
            case SERVICE_UNAVAILABLE ->
                    "Зависимый сервис для пути " + path + " сейчас недоступен. Повторите запрос чуть позже.";
            case NOT_FOUND -> "Не найден маршрут или инстанс сервиса для пути " + path;
            case GATEWAY_TIMEOUT -> "Превышено время ожидания ответа сервиса для пути " + path;
            default -> ex.getMessage() == null || ex.getMessage().isBlank()
                    ? "Шлюз не смог обработать запрос к " + path
                    : ex.getMessage();
        };
    }
}
