package ifmo.poster.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ifmo.poster.order.entity.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    Optional<OrderItem> findByOrderIdAndEventIdAndTicketTypeId(
            Long orderId, 
            Long eventId, 
            Long ticketTypeId
    );
    
    void deleteByOrderId(Long orderId);
}
