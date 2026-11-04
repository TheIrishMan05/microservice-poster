package ifmo.poster.event.dto;

import ifmo.poster.contracts.enums.WaitlistStatus;
import jakarta.validation.constraints.NotNull;

public record CreateWaitlistRequest(
        @NotNull Long userId,
        @NotNull Long eventId,
        @NotNull Long ticketTypeId,
        WaitlistStatus status
) {
}
