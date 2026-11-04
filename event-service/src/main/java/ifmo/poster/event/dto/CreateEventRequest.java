package ifmo.poster.event.dto;

import ifmo.poster.contracts.enums.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record CreateEventRequest(
        @NotBlank @Size(max = 100) String eventName,
        @NotBlank @Size(max = 200) String description,
        @NotBlank @Size(max = 200) String location,
        @NotNull LocalDateTime dateTime,
        EventStatus status,
        @Size(max = 500) String blockReason,
        List<@NotBlank @Size(max = 50) String> tagNames
) {
}
