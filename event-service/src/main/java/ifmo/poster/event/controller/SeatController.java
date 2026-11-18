package ifmo.poster.event.controller;

import ifmo.poster.contracts.support.Pagination;
import ifmo.poster.event.dto.SeatResponse;
import ifmo.poster.event.service.SeatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Каталог мест события для UI перед заказом.
 * Свободное место: нет {@code reservationId} и нет {@code ticketId}.
 */
@RestController
@RequestMapping("/api/events/{eventId}/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping
    public Mono<List<SeatResponse>> findByEvent(
            @PathVariable Long eventId,
            @RequestParam(required = false) Long ticketTypeId,
            @RequestParam(defaultValue = "false") boolean availableOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_PAGE_SIZE) int size,
            ServerHttpResponse response
    ) {
        return seatService.findByEvent(eventId, ticketTypeId, availableOnly, page, size)
                .doOnNext(tuple -> response.getHeaders()
                        .add(Pagination.TOTAL_COUNT_HEADER, String.valueOf(tuple.getT2())))
                .map(tuple -> tuple.getT1());
    }
}
