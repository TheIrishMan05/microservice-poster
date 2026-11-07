package ifmo.poster.event.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("ticket_reservation_holds")
@Getter
@Setter
@NoArgsConstructor
public class TicketReservationHold {

    @Id
    @NotNull
    private Long id;

    @Column("reservation_id")
    @NotNull 
    private Long reservationId;

    public TicketReservationHold(Long reservationId) {
        this.reservationId = reservationId;
    }
}
