package ifmo.poster.event.dto;

import ifmo.poster.contracts.enums.EventStatus;
import java.time.LocalDateTime;
import java.util.List;

public record EventResponse(
        Long id,
        String eventName,
        String description,
        String location,
        LocalDateTime dateTime,
        EventStatus status,
        String blockReason,
        List<TagResponse> tags
) {
}
