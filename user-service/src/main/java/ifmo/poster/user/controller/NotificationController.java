package ifmo.poster.user.controller;

import ifmo.poster.user.dto.CreateNotificationRequest;
import ifmo.poster.user.dto.NotificationResponse;
import ifmo.poster.user.dto.UpdateNotificationRequest;
import ifmo.poster.user.service.NotificationService;
import ifmo.poster.contracts.support.Pagination;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
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
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public Mono<ResponseEntity<NotificationResponse>> create(
            @Valid @RequestBody CreateNotificationRequest request
    ) {
        return notificationService.create(request)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<NotificationResponse>> getById(@PathVariable Long id) {
        return notificationService.getById(id).map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<List<NotificationResponse>>> findAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_PAGE_SIZE) int size
    ) {
        return notificationService.findAll(userId, page, size)
                .map(this::toPagedResponse);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<NotificationResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNotificationRequest request
    ) {
        return notificationService.update(id, request).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return notificationService.delete(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    private ResponseEntity<List<NotificationResponse>> toPagedResponse(Page<NotificationResponse> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(Pagination.TOTAL_COUNT_HEADER, String.valueOf(page.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
