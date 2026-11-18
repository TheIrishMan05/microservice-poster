package ifmo.poster.order.client;

import ifmo.poster.contracts.event.EventBriefDto;
import ifmo.poster.contracts.event.ReserveTicketsRequest;
import ifmo.poster.contracts.event.TicketReservationDto;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

/**
 * Reactive Feign-клиент к event-service (R2DBC / WebFlux).
 * Вызовы неблокирующие: возвращают {@link Mono}, без boundedElastic-обёртки.
 */
@ReactiveFeignClient(
        name = "event-service",
        url = "${clients.event-service.url:}",
        path = "/internal/events"
)
public interface EventClient {

    @GetMapping("/{id}")
    Mono<EventBriefDto> getById(@PathVariable("id") Long id);

    @PostMapping("/reservations")
    Mono<TicketReservationDto> reserve(@RequestBody ReserveTicketsRequest request);

    @DeleteMapping("/reservations/{reservationId}")
    Mono<Void> cancelReservation(@PathVariable("reservationId") String reservationId);
}
