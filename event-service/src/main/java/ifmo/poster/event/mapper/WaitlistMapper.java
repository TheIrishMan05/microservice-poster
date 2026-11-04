package ifmo.poster.event.mapper;

import ifmo.poster.contracts.enums.WaitlistStatus;
import ifmo.poster.event.dto.CreateWaitlistRequest;
import ifmo.poster.event.dto.UpdateWaitlistRequest;
import ifmo.poster.event.dto.WaitlistResponse;
import ifmo.poster.event.entity.TicketWaitlist;

public final class WaitlistMapper {

    private WaitlistMapper() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static TicketWaitlist toEntity(CreateWaitlistRequest request) {
        WaitlistStatus status = request.status() == null ? WaitlistStatus.WAITING : request.status();
        return new TicketWaitlist(request.userId(), request.eventId(), request.ticketTypeId(), status);
    }

    public static void updateEntity(TicketWaitlist waitlist, UpdateWaitlistRequest request) {
        waitlist.setStatus(request.status());
    }

    public static WaitlistResponse toResponse(TicketWaitlist waitlist) {
        return new WaitlistResponse(
                waitlist.getId(),
                waitlist.getUserId(),
                waitlist.getEventId(),
                waitlist.getTicketTypeId(),
                waitlist.getStatus(),
                waitlist.getCreatedAt()
        );
    }
}
