package ifmo.poster.order.controller;

import ifmo.poster.contracts.enums.TicketStatus;
import ifmo.poster.contracts.support.Pagination;
import ifmo.poster.order.dto.TicketResponse;
import ifmo.poster.order.service.TicketService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/{id}")
    public Mono<TicketResponse> getById(@PathVariable Long id) {
        return ticketService.getById(id);
    }

    @GetMapping
    public Mono<List<TicketResponse>> findAll(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_PAGE_SIZE) int size,
            ServerHttpResponse response
    ) {
        return ticketService.findAll(eventId, status, page, size)
                .doOnNext(result -> response.getHeaders()
                        .add(Pagination.TOTAL_COUNT_HEADER, String.valueOf(result.getTotalElements())))
                .map(Page::getContent);
    }
}
