package ifmo.poster.event.dto;

import ifmo.poster.contracts.enums.RegistrationStatus;
import jakarta.validation.constraints.NotNull;

public record CreateRegistrationRequest(
        @NotNull Long userId,
        @NotNull Long eventId,
        RegistrationStatus status
) {
}
