package ifmo.poster.event.entity;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("event_tags")
@Getter
@Setter
@NoArgsConstructor
public class EventTag {
    @Id
    @NotNull 
    private Long id; 

    @Column("event_id")
    @NotNull 
    private Long eventId;

    @Column("tag_id")
    @NotNull    
    private Long tagId;

    public EventTag(Long eventId, Long tagId) {
        this.eventId = eventId;
        this.tagId = tagId;
    }
}