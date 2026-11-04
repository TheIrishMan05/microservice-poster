package ifmo.poster.event.mapper;

import ifmo.poster.event.dto.CreateTicketTypeRequest;
import ifmo.poster.event.dto.TicketTypeResponse;
import ifmo.poster.event.dto.UpdateTicketTypeRequest;
import ifmo.poster.event.entity.TicketType;

public final class TicketTypeMapper {

    private TicketTypeMapper() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static TicketType toEntity(CreateTicketTypeRequest request) {
        TicketType ticketType = new TicketType();
        ticketType.setTypeName(request.typeName().trim());
        ticketType.setPrice(request.price());
        return ticketType;
    }

    public static void updateEntity(TicketType ticketType, UpdateTicketTypeRequest request) {
        ticketType.setTypeName(request.typeName().trim());
        ticketType.setPrice(request.price());
    }

    public static TicketTypeResponse toResponse(TicketType ticketType) {
        return new TicketTypeResponse(ticketType.getId(), ticketType.getTypeName(), ticketType.getPrice());
    }
}
