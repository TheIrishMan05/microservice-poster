package ifmo.poster.event.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("tags")
@Getter
@Setter
public class Tag {

    @Id
    @NotNull 
    private Long id;

    @Column("name")
    @Size(max = 100)
    @NotNull 
    private String name;
}