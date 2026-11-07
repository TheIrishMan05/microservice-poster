package ifmo.poster.event.entity;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("seats")
@Getter
@Setter
public class Seat {

    @Id
    @NotNull 
    private Long id;

    @Column("ticket_id")
    private Long ticketId;

    @Column("event_id")
    private Long eventId;

    @Column("ticket_type_id")
    private Long ticketTypeId;

    @Column("reservation_id")
    private String reservationId;

    @Column("row_label")
    @Size(max = 10)
    private String row;

    @Column("seat_number")
    @NotNull 
    private int number;

    @Column("sector_name")
    @Size(max = 50)
    private String sectorName;

    @Column("x_coordinate")
    private Double xCoordinate;

    @Column("y_coordinate")
    private Double yCoordinate;
}
