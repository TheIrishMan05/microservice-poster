package ifmo.poster.order.mapper;

import ifmo.poster.order.dto.OrderItemResponse;
import ifmo.poster.order.dto.OrderResponse;
import ifmo.poster.order.dto.TicketResponse;
import ifmo.poster.order.entity.Order;
import ifmo.poster.order.entity.OrderItem;
import ifmo.poster.order.entity.Ticket;
import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderMapper::toItemResponse)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getCreatedAt(),
                items
        );
    }

    public static OrderItemResponse toItemResponse(OrderItem item) {
        List<TicketResponse> tickets = item.getTickets().stream()
                .map(OrderMapper::toTicketResponse)
                .toList();
        return new OrderItemResponse(
                item.getId(),
                item.getEventId(),
                item.getTicketTypeId(),
                item.getQuantity(),
                item.getPrice(),
                item.getReservationId(),
                tickets
        );
    }

    public static TicketResponse toTicketResponse(Ticket ticket) {
        Long orderItemId = ticket.getOrderItem() == null ? null : ticket.getOrderItem().getId();
        return new TicketResponse(
                ticket.getId(),
                orderItemId,
                ticket.getEventId(),
                ticket.getTicketTypeId(),
                ticket.getStatus()
        );
    }
}
