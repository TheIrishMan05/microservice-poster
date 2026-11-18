package ifmo.poster.event.controller;

import ifmo.poster.contracts.support.Pagination;
import ifmo.poster.event.dto.CreateTagRequest;
import ifmo.poster.event.dto.TagResponse;
import ifmo.poster.event.dto.UpdateTagRequest;
import ifmo.poster.event.service.TagService;
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
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TagResponse> create(@Valid @RequestBody CreateTagRequest request) {
        return tagService.create(request);
    }

    @GetMapping("/{id}")
    public Mono<TagResponse> getById(@PathVariable Long id) {
        return tagService.getById(id);
    }

    @GetMapping
    public Mono<List<TagResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_PAGE_SIZE) int size
    ) {
        return tagService.findAll(page, size);
    }

    @PutMapping("/{id}")
    public Mono<TagResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTagRequest request
    ) {
        return tagService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return tagService.delete(id);
    }
}
