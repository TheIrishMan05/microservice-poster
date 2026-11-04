package ifmo.poster.event.entity;

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
    private Long id;

    @Column("row_label")
    private String row;

    @Column("seat_number")
    private int number;

    @Column("sector_name")
    private String sectorName;

    @Column("x_coordinate")
    private Double xCoordinate;

    @Column("y_coordinate")
    private Double yCoordinate;
}