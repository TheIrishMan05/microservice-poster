package ifmo.poster.event.dto;

import ifmo.poster.contracts.enums.WaitlistStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateWaitlistRequest(
        @NotNull WaitlistStatus status
) {
}
