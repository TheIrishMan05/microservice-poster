package ifmo.poster.event.entity;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;
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
    @NotNull 
    private Long id;

    @Column("type_name")
    @NotNull 
    @Size (max = 50)
    private String typeName;


    @Column("price")
    @NotNull 
    private BigDecimal price;
}