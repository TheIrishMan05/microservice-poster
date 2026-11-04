package ifmo.poster.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequest(
        @NotNull Long eventId,
        @NotNull Long ticketTypeId,
        @Min(1) @Max(50) int quantity
) {
}
