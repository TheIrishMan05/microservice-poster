package ifmo.poster.order.controller;

import ifmo.poster.contracts.enums.TicketStatus;
import ifmo.poster.contracts.support.Pagination;
import ifmo.poster.order.dto.TicketResponse;
import ifmo.poster.order.service.TicketService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TicketResponse>> getById(@PathVariable Long id) {
        return ticketService.getById(id)
                .flatMap(body -> Mono.just(ResponseEntity.ok(body)));
    }

    @GetMapping
    public Mono<ResponseEntity<List<TicketResponse>>> findAll(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_PAGE_SIZE) int size
    ) {
        return ticketService.findAll(eventId, status, page, size)
                .flatMap(result -> Mono.just(toPagedResponse(result)));
    }

    private ResponseEntity<List<TicketResponse>> toPagedResponse(Page<TicketResponse> pageResult) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(Pagination.TOTAL_COUNT_HEADER, String.valueOf(pageResult.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(pageResult.getContent());
    }
}
