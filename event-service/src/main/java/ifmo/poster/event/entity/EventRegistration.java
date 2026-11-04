package ifmo.poster.event.entity;

import java.time.LocalDateTime;

import ifmo.poster.contracts.enums.RegistrationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("event_registrations")
@Getter
@Setter
@NoArgsConstructor 
public class EventRegistration {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("event_id")
    private Long eventId;

    @Column("registration_date")
    private LocalDateTime registrationDate;

    @Column("status")
    private RegistrationStatus status;

    public EventRegistration(Long userId, Long eventId, RegistrationStatus status) {
        this.userId = userId;
        this.eventId = eventId;
        this.registrationDate = LocalDateTime.now();
        this.status = status;
    }
}