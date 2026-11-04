package ifmo.poster.event.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("ticket_reservations")
@Getter
@Setter
@NoArgsConstructor
public class TicketReservation {

    @Id
    private Long id;

    @Column("public_id")
    private String publicId;

    @Column("event_id")
    private Long eventId;

    @Column("ticket_type_id")
    private Long ticketTypeId;

    @Column("quantity")
    private int quantity;

    @Column("unit_price")
    private BigDecimal unitPrice;

    @Column("status")
    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;

    public TicketReservation(
            String publicId,
            Long eventId,
            Long ticketTypeId,
            int quantity,
            BigDecimal unitPrice
    ) {
        this.publicId = publicId;
        this.eventId = eventId;
        this.ticketTypeId = ticketTypeId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }
}
