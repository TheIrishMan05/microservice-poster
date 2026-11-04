package ifmo.poster.event.dto;

import ifmo.poster.contracts.enums.RegistrationStatus;
import java.time.LocalDateTime;

public record RegistrationResponse(
        Long id,
        Long userId,
        Long eventId,
        LocalDateTime registrationDate,
        RegistrationStatus status
) {
}
