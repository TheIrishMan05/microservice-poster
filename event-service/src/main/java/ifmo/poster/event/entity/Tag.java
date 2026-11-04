package ifmo.poster.event.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("tags")
@Getter
@Setter
public class Tag {

    @Id
    private Long id;

    @Column("name")
    private String name;
}