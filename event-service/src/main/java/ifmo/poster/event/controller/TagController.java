package ifmo.poster.event.controller;

import ifmo.poster.event.dto.CreateTagRequest;
import ifmo.poster.event.dto.TagResponse;
import ifmo.poster.event.dto.UpdateTagRequest;
import ifmo.poster.event.service.TagService;
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
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public Mono<ResponseEntity<TagResponse>> create(@Valid @RequestBody CreateTagRequest request) {
        return tagService.create(request)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TagResponse>> getById(@PathVariable Long id) {
        return tagService.getById(id).map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<List<TagResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_PAGE_SIZE) int size
    ) {
        return tagService.findAll(page, size).map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<TagResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTagRequest request
    ) {
        return tagService.update(id, request).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return tagService.delete(id).thenReturn(ResponseEntity.noContent().build());
    }
}
