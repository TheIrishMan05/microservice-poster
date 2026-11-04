package ifmo.poster.event.entity;

import ifmo.poster.contracts.enums.EventStatus;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id; 
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Getter;
import lombok.Setter;



@Table("events")
@Getter
@Setter
public class Event {

    @Id
    private Long id;

    @Column("event_name")
    private String eventName;

    @Column("description")
    private String description;

    @Column("location")
    private String location;

    @Column("date_time")
    private LocalDateTime dateTime;

    @Column("status")
    private EventStatus status = EventStatus.PENDING;

    @Column("block_reason")
    private String blockReason;
}