package ifmo.poster.event.controller;

import ifmo.poster.contracts.enums.EventStatus;
import ifmo.poster.contracts.support.Pagination;
import ifmo.poster.event.dto.BulkCreateEventsRequest;
import ifmo.poster.event.dto.CreateEventRequest;
import ifmo.poster.event.dto.EventResponse;
import ifmo.poster.event.dto.UpdateEventRequest;
import ifmo.poster.event.service.EventService;
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
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        return eventService.create(request);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<List<EventResponse>> createBulk(@Valid @RequestBody BulkCreateEventsRequest request) {
        return eventService.createBulk(request.events());
    }

    @DeleteMapping("/data")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> clearAllData() {
        return eventService.clearAllData();
    }

    @GetMapping("/{id}")
    public Mono<EventResponse> getById(@PathVariable Long id) {
        return eventService.getById(id);
    }

    /**
     * Infinite scroll without total count: cursor by {@code afterId}.
     */
    @GetMapping
    public Mono<List<EventResponse>> findAll(
            @RequestParam(defaultValue = "0") Long afterId,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_PAGE_SIZE) int limit,
            @RequestParam(required = false) EventStatus status
    ) {
        return eventService.findNextBatch(afterId, limit, status);
    }

    @PutMapping("/{id}")
    public Mono<EventResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request
    ) {
        return eventService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return eventService.delete(id);
    }
}
