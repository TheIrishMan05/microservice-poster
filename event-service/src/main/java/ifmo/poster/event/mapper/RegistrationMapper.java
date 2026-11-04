package ifmo.poster.event.mapper;

import ifmo.poster.contracts.enums.RegistrationStatus;
import ifmo.poster.event.dto.CreateRegistrationRequest;
import ifmo.poster.event.dto.RegistrationResponse;
import ifmo.poster.event.dto.UpdateRegistrationRequest;
import ifmo.poster.event.entity.EventRegistration;

public final class RegistrationMapper {

    private RegistrationMapper() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static EventRegistration toEntity(CreateRegistrationRequest request) {
        RegistrationStatus status = request.status() == null ? RegistrationStatus.CONFIRMED : request.status();
        return new EventRegistration(request.userId(), request.eventId(), status);
    }

    public static void updateEntity(EventRegistration registration, UpdateRegistrationRequest request) {
        registration.setStatus(request.status());
    }

    public static RegistrationResponse toResponse(EventRegistration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getUserId(),
                registration.getEventId(),
                registration.getRegistrationDate(),
                registration.getStatus()
        );
    }
}
