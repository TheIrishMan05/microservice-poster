package ifmo.poster.order.service;

import ifmo.poster.contracts.event.EventBriefDto;
import ifmo.poster.contracts.event.ReserveTicketsRequest;
import ifmo.poster.contracts.event.TicketReservationDto;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.order.client.EventClient;
import ifmo.poster.order.client.RemoteCallErrors;
import ifmo.poster.order.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EventClientService {

    private final EventClient eventClient;

    @Retry(name = "eventService")
    @CircuitBreaker(name = "eventService", fallbackMethod = "getByIdFallback")
    public Mono<EventBriefDto> getById(Long eventId) {
        return eventClient.getById(eventId)
                .onErrorMap(error -> RemoteCallErrors.mapNotFound(
                        error,
                        "Мероприятие не найдено: id=" + eventId
                ));
    }

    @Retry(name = "eventService")
    @CircuitBreaker(name = "eventService", fallbackMethod = "reserveFallback")
    public Mono<TicketReservationDto> reserve(ReserveTicketsRequest request) {
        return eventClient.reserve(request)
                .onErrorMap(error -> RemoteCallErrors.mapNotFound(
                        error,
                        "Не удалось зарезервировать: событие или тип билета не найдены"
                ));
    }

    @Retry(name = "eventService")
    @CircuitBreaker(name = "eventService", fallbackMethod = "cancelReservationFallback")
    public Mono<Void> cancelReservation(String reservationId) {
        return eventClient.cancelReservation(reservationId)
                .onErrorResume(error -> RemoteCallErrors.isNotFound(error)
                        ? Mono.empty()
                        : Mono.error(error));
    }

    private Mono<EventBriefDto> getByIdFallback(Long eventId, Throwable cause) {
        if (RemoteCallErrors.isNotFound(cause)) {
            return Mono.error(new ResourceNotFoundException("Мероприятие не найдено: id=" + eventId));
        }
        return unavailable("Не удалось получить мероприятие. eventId=" + eventId, cause);
    }

    private Mono<TicketReservationDto> reserveFallback(ReserveTicketsRequest request, Throwable cause) {
        if (RemoteCallErrors.isNotFound(cause)) {
            return Mono.error(new ResourceNotFoundException(
                    "Не удалось зарезервировать: событие или тип билета не найдены"
            ));
        }
        return unavailable("Не удалось зарезервировать билеты для eventId=" + request.eventId(), cause);
    }

    private Mono<Void> cancelReservationFallback(String reservationId, Throwable cause) {
        if (RemoteCallErrors.isNotFound(cause)) {
            return Mono.empty();
        }
        return unavailable("Не удалось отменить резерв " + reservationId, cause);
    }

    private <T> Mono<T> unavailable(String message, Throwable cause) {
        return Mono.error(new ServiceUnavailableException(message, cause));
    }
}
