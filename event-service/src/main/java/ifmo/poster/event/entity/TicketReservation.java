package ifmo.poster.event.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("ticket_reservations")
@Getter
@Setter
@NoArgsConstructor
public class TicketReservation {

    @Id
    @NotNull 
    private Long id;

    @Column("public_id")
    @Size (max = 36)
    @NotNull 
    private String publicId;

    @Column("event_id")
    @NotNull 
    private Long eventId;

    @Column("ticket_type_id")
    @NotNull 
    private Long ticketTypeId;

    @Column("quantity")
    @NotNull
    private int quantity;

    @Column("unit_price")
    @NotNull 
    private BigDecimal unitPrice;

    @Column("status")
    @NotNull 
    @Size (max = 20)
    private String status;

    @Column("created_at")
    @NotNull 
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
