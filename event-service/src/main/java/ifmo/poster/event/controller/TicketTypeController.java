package ifmo.poster.event.controller;

import ifmo.poster.event.dto.CreateTicketTypeRequest;
import ifmo.poster.event.dto.TicketTypeResponse;
import ifmo.poster.event.dto.UpdateTicketTypeRequest;
import ifmo.poster.event.service.TicketTypeService;
import ifmo.poster.contracts.support.Pagination;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ticket-types")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    public TicketTypeController(TicketTypeService ticketTypeService) {
        this.ticketTypeService = ticketTypeService;
    }

    @PostMapping
    public Mono<ResponseEntity<TicketTypeResponse>> create(
            @Valid @RequestBody CreateTicketTypeRequest request
    ) {
        return ticketTypeService.create(request)
                .flatMap(body -> Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(body)));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TicketTypeResponse>> getById(@PathVariable Long id) {
        return ticketTypeService.getById(id)
                .flatMap(body -> Mono.just(ResponseEntity.ok(body)));
    }

    @GetMapping
    public Mono<ResponseEntity<List<TicketTypeResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_PAGE_SIZE) int size
    ) {
        return ticketTypeService.findAll(page, size)
                .flatMap(body -> Mono.just(ResponseEntity.ok(body)));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<TicketTypeResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketTypeRequest request
    ) {
        return ticketTypeService.update(id, request)
                .flatMap(body -> Mono.just(ResponseEntity.ok(body)));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return ticketTypeService.delete(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
