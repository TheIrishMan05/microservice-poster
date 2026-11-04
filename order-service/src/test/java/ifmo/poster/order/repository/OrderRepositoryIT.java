package ifmo.poster.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ifmo.poster.contracts.enums.OrderStatus;
import ifmo.poster.contracts.enums.TicketStatus;
import ifmo.poster.order.entity.Order;
import ifmo.poster.order.entity.OrderItem;
import ifmo.poster.order.entity.Ticket;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import ifmo.poster.order.client.EventClient;
import ifmo.poster.order.client.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Testcontainers
class OrderRepositoryIT {

    @MockitoBean
    UserClient userClient;

    @MockitoBean
    EventClient eventClient;

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:15-alpine");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    OrderRepository orderRepository;

    @Test
    @Transactional
    void findDetailedByIdLoadsItemsAndTickets() {
        Order order = new Order();
        order.setUserId(42L);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setEventId(10L);
        item.setTicketTypeId(1L);
        item.setQuantity(2);
        item.setPrice(new BigDecimal("2000.00"));
        item.setReservationId(UUID.randomUUID().toString());

        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Ticket ticket = new Ticket();
            ticket.setOrderItem(item);
            ticket.setEventId(10L);
            ticket.setTicketTypeId(1L);
            ticket.setStatus(TicketStatus.RESERVED);
            tickets.add(ticket);
        }
        item.setTickets(tickets);
        order.setItems(List.of(item));

        Order saved = orderRepository.saveAndFlush(order);

        Order loaded = orderRepository.findDetailedById(saved.getId()).orElseThrow();

        assertThat(loaded.getItems()).hasSize(1);
        assertThat(loaded.getItems().getFirst().getReservationId()).isEqualTo(item.getReservationId());
        assertThat(loaded.getItems().getFirst().getTickets()).hasSize(2);
        assertThat(loaded.getItems().getFirst().getTickets())
                .allMatch(ticket -> ticket.getStatus() == TicketStatus.RESERVED);
    }
}
