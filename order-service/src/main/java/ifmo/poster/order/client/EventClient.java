package ifmo.poster.order.client;

import ifmo.poster.contracts.event.EventBriefDto;
import ifmo.poster.contracts.event.ReserveTicketsRequest;
import ifmo.poster.contracts.event.TicketReservationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "event-service", url = "${clients.event-service.url:}", path = "/internal/events")
public interface EventClient {

    @GetMapping("/{id}")
    EventBriefDto getById(@PathVariable("id") Long id);

    @PostMapping("/reservations")
    TicketReservationDto reserve(@RequestBody ReserveTicketsRequest request);

    @DeleteMapping("/reservations/{reservationId}")
    void cancelReservation(@PathVariable("reservationId") String reservationId);
}
