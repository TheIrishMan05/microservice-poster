package ifmo.poster.order.controller;

import ifmo.poster.contracts.support.Pagination;
import ifmo.poster.order.dto.BulkCreateOrdersRequest;
import ifmo.poster.order.dto.CreateOrderRequest;
import ifmo.poster.order.dto.OrderResponse;
import ifmo.poster.order.dto.UpdateOrderStatusRequest;
import ifmo.poster.order.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<List<OrderResponse>> createBulk(@Valid @RequestBody BulkCreateOrdersRequest request) {
        return orderService.createBulk(request.orders());
    }

    @DeleteMapping("/data")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> clearAllData() {
        return orderService.clearAllData();
    }

    @GetMapping("/{id}")
    public Mono<OrderResponse> getById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @GetMapping
    public Mono<List<OrderResponse>> findAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_PAGE_SIZE) int size,
            ServerHttpResponse response
    ) {
        return orderService.findAll(userId, page, size)
                .doOnNext(result -> response.getHeaders()
                        .add(Pagination.TOTAL_COUNT_HEADER, String.valueOf(result.getTotalElements())))
                .map(Page::getContent);
    }

    @PutMapping("/{id}/status")
    public Mono<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return orderService.updateStatus(id, request);
    }
}
