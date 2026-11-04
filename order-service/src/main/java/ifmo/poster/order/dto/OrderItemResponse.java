package ifmo.poster.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderItemResponse(
        Long id,
        Long eventId,
        Long ticketTypeId,
        int quantity,
        BigDecimal price,
        String reservationId,
        List<TicketResponse> tickets
) {
}
