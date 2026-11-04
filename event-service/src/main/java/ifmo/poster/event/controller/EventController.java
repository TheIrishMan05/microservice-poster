package ifmo.poster.event.controller;

import ifmo.poster.contracts.enums.EventStatus;
import ifmo.poster.event.dto.CreateEventRequest;
import ifmo.poster.event.dto.EventResponse;
import ifmo.poster.event.dto.UpdateEventRequest;
import ifmo.poster.event.service.EventService;
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
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public Mono<ResponseEntity<EventResponse>> create(@Valid @RequestBody CreateEventRequest request) {
        return eventService.create(request)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<EventResponse>> getById(@PathVariable Long id) {
        return eventService.getById(id).map(ResponseEntity::ok);
    }

    /**
     * Infinite scroll without total count: cursor by {@code afterId}.
     */
    @GetMapping
    public Mono<ResponseEntity<List<EventResponse>>> findAll(
            @RequestParam(defaultValue = "0") Long afterId,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_PAGE_SIZE) int limit,
            @RequestParam(required = false) EventStatus status
    ) {
        return eventService.findNextBatch(afterId, limit, status).map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<EventResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request
    ) {
        return eventService.update(id, request).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return eventService.delete(id).thenReturn(ResponseEntity.noContent().build());
    }
}
