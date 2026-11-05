package ifmo.poster.event.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkCreateEventsRequest(
        @NotEmpty List<@Valid CreateEventRequest> events
) {
}
