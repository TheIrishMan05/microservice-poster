package ifmo.poster.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkCreateOrdersRequest(
        @NotEmpty List<@Valid CreateOrderRequest> orders
) {
}
