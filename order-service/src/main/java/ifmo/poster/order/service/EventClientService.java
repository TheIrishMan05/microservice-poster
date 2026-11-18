package ifmo.poster.order.service;

import ifmo.poster.contracts.event.EventBriefDto;
import ifmo.poster.contracts.event.ReserveTicketsRequest;
import ifmo.poster.contracts.event.TicketReservationDto;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.order.client.EventClient;
import ifmo.poster.order.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactivefeign.client.ReactiveFeignException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EventClientService {

    private final EventClient eventClient;

    @Retry(name = "eventService", fallbackMethod = "getByIdFallback")
    @CircuitBreaker(name = "eventService", fallbackMethod = "getByIdFallback")
    public Mono<EventBriefDto> getById(Long eventId) {
        return eventClient.getById(eventId);
    }

    @Retry(name = "eventService", fallbackMethod = "reserveFallback")
    @CircuitBreaker(name = "eventService", fallbackMethod = "reserveFallback")
    public Mono<TicketReservationDto> reserve(ReserveTicketsRequest request) {
        return eventClient.reserve(request);
    }

    @Retry(name = "eventService", fallbackMethod = "cancelReservationFallback")
    @CircuitBreaker(name = "eventService", fallbackMethod = "cancelReservationFallback")
    public Mono<Void> cancelReservation(String reservationId) {
        return eventClient.cancelReservation(reservationId);
    }

    private Mono<EventBriefDto> getByIdFallback(Long eventId, Throwable cause) {
        if (isNotFound(cause)) {
            return Mono.error(new ResourceNotFoundException("Мероприятие не найдено: id=" + eventId));
        }
        return unavailable("Не удалось получить мероприятие. eventId=" + eventId, cause);
    }

    private Mono<TicketReservationDto> reserveFallback(ReserveTicketsRequest request, Throwable cause) {
        if (isNotFound(cause)) {
            return Mono.error(new ResourceNotFoundException(
                    "Не удалось зарезервировать: событие или тип билета не найдены"
            ));
        }
        return unavailable("Не удалось зарезервировать билеты для eventId=" + request.eventId(), cause);
    }

    private Mono<Void> cancelReservationFallback(String reservationId, Throwable cause) {
        if (isNotFound(cause)) {
            return Mono.empty();
        }
        return unavailable("Не удалось отменить резерв " + reservationId, cause);
    }

    private <T> Mono<T> unavailable(String message, Throwable cause) {
        return Mono.error(new ServiceUnavailableException(message, cause));
    }

    private static boolean isNotFound(Throwable cause) {
        Throwable current = cause;
        while (current != null) {
            if (current instanceof WebClientResponseException webClientException
                    && webClientException.getStatusCode().value() == 404) {
                return true;
            }
            if (current instanceof ReactiveFeignException reactiveFeignException) {
                HttpStatusCode status = extractStatus(reactiveFeignException);
                if (status != null && status.value() == 404) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private static HttpStatusCode extractStatus(ReactiveFeignException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof WebClientResponseException webClientException) {
            return webClientException.getStatusCode();
        }
        return null;
    }
}
