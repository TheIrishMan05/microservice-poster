package ifmo.poster.order.dto;

import ifmo.poster.contracts.enums.TicketStatus;

public record TicketResponse(
        Long id,
        Long orderItemId,
        Long eventId,
        Long ticketTypeId,
        TicketStatus status
) {
}
