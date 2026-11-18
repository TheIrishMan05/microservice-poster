package ifmo.poster.event.controller;

import ifmo.poster.contracts.support.Pagination;
import ifmo.poster.event.dto.CreateRegistrationRequest;
import ifmo.poster.event.dto.RegistrationResponse;
import ifmo.poster.event.dto.UpdateRegistrationRequest;
import ifmo.poster.event.service.RegistrationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
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
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RegistrationResponse> create(@Valid @RequestBody CreateRegistrationRequest request) {
        return registrationService.create(request);
    }

    @GetMapping("/{id}")
    public Mono<RegistrationResponse> getById(@PathVariable Long id) {
        return registrationService.getById(id);
    }

    @GetMapping
    public Mono<List<RegistrationResponse>> findAll(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_PAGE_SIZE) int size,
            ServerHttpResponse response
    ) {
        return registrationService.findAll(eventId, userId, page, size)
                .doOnNext(tuple -> response.getHeaders()
                        .add(Pagination.TOTAL_COUNT_HEADER, String.valueOf(tuple.getT2())))
                .map(tuple -> tuple.getT1());
    }

    @PutMapping("/{id}")
    public Mono<RegistrationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRegistrationRequest request
    ) {
        return registrationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return registrationService.delete(id);
    }
}
