package ifmo.poster.event.controller;

import ifmo.poster.contracts.event.EventBriefDto;
import ifmo.poster.contracts.event.ReserveTicketsRequest;
import ifmo.poster.contracts.event.TicketReservationDto;
import ifmo.poster.event.service.EventService;
import ifmo.poster.event.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/events")
public class InternalEventController {

    private final EventService eventService;
    private final ReservationService reservationService;

    public InternalEventController(EventService eventService, ReservationService reservationService) {
        this.eventService = eventService;
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<EventBriefDto>> getById(@PathVariable Long id) {
        return eventService.getBriefById(id).map(ResponseEntity::ok);
    }

    @PostMapping("/reservations")
    public Mono<ResponseEntity<TicketReservationDto>> reserve(
            @RequestBody ReserveTicketsRequest request
    ) {
        return reservationService.reserve(request)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @DeleteMapping("/reservations/{reservationId}")
    public Mono<ResponseEntity<Void>> cancelReservation(@PathVariable String reservationId) {
        return reservationService.cancel(reservationId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
