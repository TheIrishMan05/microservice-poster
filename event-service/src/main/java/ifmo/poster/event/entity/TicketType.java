package ifmo.poster.event.entity;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;



import lombok.Getter;
import lombok.Setter;


@Table("ticket_types")
@Getter
@Setter
public class TicketType {

    @Id
    private Long id;

    @Column("type_name")
    private String typeName;


    @Column("price")
    private BigDecimal price;
}