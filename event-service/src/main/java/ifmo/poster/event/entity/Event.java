package ifmo.poster.event.entity;

import ifmo.poster.contracts.enums.EventStatus;
import jakarta.validation.constraints.*;

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
    @NotNull 
    private Long id;

    @Column("event_name")
    @Size(max = 100)
    @NotNull
    private String eventName;

    @Column("description")
    @Size(max = 200)
    private String description;

    @Column("location")
    @Size(max = 200)
    @NotNull
    private String location;

    @Column("date_time")
    @NotNull
    private LocalDateTime dateTime;

    @Column("status")
    @Size(max = 20)
    @NotNull
    private EventStatus status = EventStatus.PENDING;

    @Column("block_reason")
    @Size (max = 500)
    private String blockReason;
}