package ifmo.poster.order.service;

import ifmo.poster.contracts.enums.OrderStatus;
import ifmo.poster.contracts.enums.TicketStatus;
import ifmo.poster.contracts.event.TicketReservationDto;
import ifmo.poster.contracts.exception.ConflictException;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.order.dto.CreateOrderItemRequest;
import ifmo.poster.order.dto.CreateOrderRequest;
import ifmo.poster.order.entity.Order;
import ifmo.poster.order.entity.OrderItem;
import ifmo.poster.order.entity.Ticket;
import ifmo.poster.order.mapper.OrderMapper;
import ifmo.poster.order.dto.OrderResponse;
import ifmo.poster.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Локальные JPA-операции заказа. Выделены отдельно, чтобы {@code @Transactional}
 * срабатывал через Spring proxy (не self-invocation из reactive-оркестратора).
 */
@Service
public class OrderPersistenceService {

    private final OrderRepository orderRepository;

    public OrderPersistenceService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse createPendingOrder(CreateOrderRequest request, List<TicketReservationDto> reservations) {
        if (request.items().size() != reservations.size()) {
            throw new IllegalStateException("Количество резервов не совпадает с позициями заказа");
        }

        Order order = new Order();
        order.setUserId(request.userId());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < request.items().size(); i++) {
            CreateOrderItemRequest itemRequest = request.items().get(i);
            TicketReservationDto reservation = reservations.get(i);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setEventId(itemRequest.eventId());
            item.setTicketTypeId(itemRequest.ticketTypeId());
            item.setQuantity(itemRequest.quantity());
            item.setPrice(reservation.unitPrice());
            item.setReservationId(reservation.reservationId());

            List<Ticket> tickets = new ArrayList<>();
            for (int t = 0; t < itemRequest.quantity(); t++) {
                Ticket ticket = new Ticket();
                ticket.setOrderItem(item);
                ticket.setEventId(itemRequest.eventId());
                ticket.setTicketTypeId(itemRequest.ticketTypeId());
                ticket.setStatus(TicketStatus.RESERVED);
                tickets.add(ticket);
            }
            item.setTickets(tickets);
            items.add(item);
        }
        order.setItems(items);

        Order saved = orderRepository.save(order);
        return OrderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return OrderMapper.toResponse(requireDetailed(id));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(Long userId, int page, int size) {
        ifmo.poster.contracts.support.Pagination.validate(page, size);
        PageRequest pageable = PageRequest.of(page, size);
        Page<Order> orders = userId == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findByUserId(userId, pageable);
        return orders.map(OrderMapper::toResponse);
    }

    @Transactional
    public OrderResponse confirm(Long id) {
        Order order = requireDetailed(id);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException("Нельзя подтвердить отменённый заказ: id=" + id);
        }
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            return OrderMapper.toResponse(order);
        }
        order.setStatus(OrderStatus.CONFIRMED);
        order.getItems().forEach(item -> item.getTickets()
                .forEach(ticket -> ticket.setStatus(TicketStatus.SOLD)));
        return OrderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse markCancelled(Long id) {
        Order order = requireDetailed(id);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return OrderMapper.toResponse(order);
        }
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            throw new ConflictException("Подтверждённый заказ нельзя отменить: id=" + id);
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.getItems().forEach(item -> item.getTickets()
                .forEach(ticket -> ticket.setStatus(TicketStatus.AVAILABLE)));
        return OrderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<String> reservationIds(Long orderId) {
        return requireDetailed(orderId).getItems().stream()
                .map(OrderItem::getReservationId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
    }

    private Order requireDetailed(Long id) {
        return orderRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден: id=" + id));
    }
}
