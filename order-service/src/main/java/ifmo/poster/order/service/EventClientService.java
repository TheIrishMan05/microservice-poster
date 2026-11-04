package ifmo.poster.order.service;

import ifmo.poster.contracts.event.EventBriefDto;
import ifmo.poster.contracts.event.ReserveTicketsRequest;
import ifmo.poster.contracts.event.TicketReservationDto;
import ifmo.poster.order.client.EventClient;
import ifmo.poster.order.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class EventClientService {

    private final EventClient eventClient;

    public EventClientService(EventClient eventClient) {
        this.eventClient = eventClient;
    }

    @Retry(name = "eventService", fallbackMethod = "getByIdFallback")
    @CircuitBreaker(name = "eventService", fallbackMethod = "getByIdFallback")
    public Mono<EventBriefDto> getById(Long eventId) {
        return blocking(() -> eventClient.getById(eventId));
    }

    @Retry(name = "eventService", fallbackMethod = "reserveFallback")
    @CircuitBreaker(name = "eventService", fallbackMethod = "reserveFallback")
    public Mono<TicketReservationDto> reserve(ReserveTicketsRequest request) {
        return blocking(() -> eventClient.reserve(request));
    }

    @Retry(name = "eventService", fallbackMethod = "cancelReservationFallback")
    @CircuitBreaker(name = "eventService", fallbackMethod = "cancelReservationFallback")
    public Mono<Void> cancelReservation(String reservationId) {
        return Mono.fromRunnable(() -> eventClient.cancelReservation(reservationId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private Mono<EventBriefDto> getByIdFallback(Long eventId, Throwable cause) {
        return unavailable("Не удалось получить мероприятие. eventId=" + eventId, cause);
    }

    private Mono<TicketReservationDto> reserveFallback(ReserveTicketsRequest request, Throwable cause) {
        return unavailable("Не удалось зарезервировать билеты для eventId=" + request.eventId(), cause);
    }

    private Mono<Void> cancelReservationFallback(String reservationId, Throwable cause) {
        return unavailable("Не удалось отменить резерв " + reservationId, cause);
    }

    private <T> Mono<T> blocking(java.util.concurrent.Callable<T> action) {
        return Mono.fromCallable(action).subscribeOn(Schedulers.boundedElastic());
    }

    private <T> Mono<T> unavailable(String message, Throwable cause) {
        return Mono.error(new ServiceUnavailableException(message, cause));
    }
}
