package ifmo.poster.event.mapper;

import ifmo.poster.event.dto.SeatResponse;
import ifmo.poster.event.entity.Seat;

public final class SeatMapper {

    private SeatMapper() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static SeatResponse toResponse(Seat seat) {
        boolean available = seat.getTicketId() == null
                && (seat.getReservationId() == null || seat.getReservationId().isBlank());
        return new SeatResponse(
                seat.getId(),
                seat.getEventId(),
                seat.getTicketTypeId(),
                seat.getRow(),
                seat.getNumber(),
                seat.getSectorName(),
                seat.getXCoordinate(),
                seat.getYCoordinate(),
                seat.getReservationId(),
                available
        );
    }
}
