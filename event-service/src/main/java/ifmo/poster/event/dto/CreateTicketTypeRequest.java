package ifmo.poster.event.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateTicketTypeRequest(
        @NotBlank @Size(max = 50) String typeName,
        @NotNull @DecimalMin("0.00") BigDecimal price
) {
}
