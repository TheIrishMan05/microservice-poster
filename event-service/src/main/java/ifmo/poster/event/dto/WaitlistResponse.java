package ifmo.poster.event.dto;

import ifmo.poster.contracts.enums.WaitlistStatus;
import java.time.LocalDateTime;

public record WaitlistResponse(
        Long id,
        Long userId,
        Long eventId,
        Long ticketTypeId,
        WaitlistStatus status,
        LocalDateTime createdAt
) {
}
