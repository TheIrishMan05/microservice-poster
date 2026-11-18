package ifmo.poster.event.controller;

import ifmo.poster.contracts.support.Pagination;
import ifmo.poster.event.dto.CreateTicketTypeRequest;
import ifmo.poster.event.dto.TicketTypeResponse;
import ifmo.poster.event.dto.UpdateTicketTypeRequest;
import ifmo.poster.event.service.TicketTypeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TicketTypeResponse> create(@Valid @RequestBody CreateTicketTypeRequest request) {
        return ticketTypeService.create(request);
    }

    @GetMapping("/{id}")
    public Mono<TicketTypeResponse> getById(@PathVariable Long id) {
        return ticketTypeService.getById(id);
    }

    @GetMapping
    public Mono<List<TicketTypeResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_PAGE_SIZE) int size
    ) {
        return ticketTypeService.findAll(page, size);
    }

    @PutMapping("/{id}")
    public Mono<TicketTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketTypeRequest request
    ) {
        return ticketTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return ticketTypeService.delete(id);
    }
}
