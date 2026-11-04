package ifmo.poster.event.dto;

import ifmo.poster.contracts.enums.RegistrationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRegistrationRequest(
        @NotNull RegistrationStatus status
) {
}
