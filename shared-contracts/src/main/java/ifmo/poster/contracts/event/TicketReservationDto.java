package ifmo.poster.contracts.event;

import java.math.BigDecimal;
import java.util.List;

public record TicketReservationDto(
        String reservationId,
        Long eventId,
        Long ticketTypeId,
        int quantity,
        BigDecimal unitPrice,
        List<Long> ticketIds
) {
    public TicketReservationDto {
        ticketIds = ticketIds == null ? List.of() : List.copyOf(ticketIds);
    }
}
