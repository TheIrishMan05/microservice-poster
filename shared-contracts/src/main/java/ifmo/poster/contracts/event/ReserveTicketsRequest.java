package ifmo.poster.contracts.event;

public record ReserveTicketsRequest(
        Long eventId,
        Long ticketTypeId,
        int quantity
) {
}
