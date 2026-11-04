package ifmo.poster.event.entity;

import ifmo.poster.contracts.enums.WaitlistStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("ticket_waitlists")
@Getter
@Setter
@NoArgsConstructor
public class TicketWaitlist {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("event_id")
    private Long eventId;

    @Column("ticket_type_id")
    private Long ticketTypeId;

    @Column("status")
    private WaitlistStatus status;

    @Column("created_at")
    private LocalDateTime createdAt;

    public TicketWaitlist(Long userId, Long eventId, Long ticketTypeId, WaitlistStatus status) {
        this.userId = userId;
        this.eventId = eventId;
        this.ticketTypeId = ticketTypeId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}