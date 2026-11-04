package ifmo.poster.event.repository;

import ifmo.poster.contracts.enums.WaitlistStatus;
import ifmo.poster.event.entity.TicketWaitlist;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TicketWaitlistRepository extends R2dbcRepository<TicketWaitlist, Long> {

    Mono<TicketWaitlist> findByUserIdAndEventIdAndTicketTypeIdAndStatus(
            Long userId,
            Long eventId,
            Long ticketTypeId,
            WaitlistStatus status
    );

    Flux<TicketWaitlist> findByEventIdAndTicketTypeIdAndStatus(
            Long eventId,
            Long ticketTypeId,
            WaitlistStatus status
    );

    Flux<TicketWaitlist> findByUserId(Long userId);

    @Query("""
            SELECT * FROM ticket_waitlists
            WHERE (:eventId IS NULL OR event_id = :eventId)
              AND (:userId IS NULL OR user_id = :userId)
            ORDER BY id ASC
            LIMIT :limit OFFSET :offset
            """)
    Flux<TicketWaitlist> findFilteredPaged(Long eventId, Long userId, int limit, long offset);

    @Query("""
            SELECT COUNT(*) FROM ticket_waitlists
            WHERE (:eventId IS NULL OR event_id = :eventId)
              AND (:userId IS NULL OR user_id = :userId)
            """)
    Mono<Long> countFiltered(Long eventId, Long userId);
}
