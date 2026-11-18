package ifmo.poster.event.dto;

public record SeatResponse(
        Long id,
        Long eventId,
        Long ticketTypeId,
        String row,
        int number,
        String sectorName,
        Double xCoordinate,
        Double yCoordinate,
        String reservationId,
        boolean available
) {
}
