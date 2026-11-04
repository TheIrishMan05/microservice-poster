package ifmo.poster.contracts.event;

import ifmo.poster.contracts.enums.EventStatus;
import java.time.LocalDateTime;

public record EventBriefDto(
        Long id,
        String name,
        String location,
        LocalDateTime dateTime,
        EventStatus status
) {
}
