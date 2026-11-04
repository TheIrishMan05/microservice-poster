package ifmo.poster.order.dto;

import ifmo.poster.contracts.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}
