package ifmo.poster.event.dto;

import java.math.BigDecimal;

public record TicketTypeResponse(
        Long id,
        String typeName,
        BigDecimal price
) {
}
