package ifmo.poster.event.mapper;

import ifmo.poster.contracts.enums.EventStatus;
import ifmo.poster.contracts.event.EventBriefDto;
import ifmo.poster.event.dto.CreateEventRequest;
import ifmo.poster.event.dto.EventResponse;
import ifmo.poster.event.dto.TagResponse;
import ifmo.poster.event.dto.UpdateEventRequest;
import ifmo.poster.event.entity.Event;
import java.util.List;

public final class EventMapper {

    private EventMapper() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static Event toEntity(CreateEventRequest request) {
        Event event = new Event();
        event.setEventName(request.eventName().trim());
        event.setDescription(request.description().trim());
        event.setLocation(request.location().trim());
        event.setDateTime(request.dateTime());
        event.setStatus(request.status() == null ? EventStatus.PENDING : request.status());
        event.setBlockReason(blankToNull(request.blockReason()));
        return event;
    }

    public static void updateEntity(Event event, UpdateEventRequest request) {
        event.setEventName(request.eventName().trim());
        event.setDescription(request.description().trim());
        event.setLocation(request.location().trim());
        event.setDateTime(request.dateTime());
        event.setStatus(request.status());
        event.setBlockReason(blankToNull(request.blockReason()));
    }

    public static EventResponse toResponse(Event event, List<TagResponse> tags) {
        return new EventResponse(
                event.getId(),
                event.getEventName(),
                event.getDescription(),
                event.getLocation(),
                event.getDateTime(),
                event.getStatus(),
                event.getBlockReason(),
                tags
        );
    }

    public static EventBriefDto toBrief(Event event) {
        return new EventBriefDto(
                event.getId(),
                event.getEventName(),
                event.getLocation(),
                event.getDateTime(),
                event.getStatus()
        );
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
