package ifmo.poster.event.repository;

import ifmo.poster.event.entity.EventRegistration;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventRegistrationRepository extends R2dbcRepository<EventRegistration, Long> {

    Mono<Boolean> existsByUserIdAndEventId(Long userId, Long eventId);

    Mono<EventRegistration> findByUserIdAndEventId(Long userId, Long eventId);

    Flux<EventRegistration> findByUserId(Long userId);

    Flux<EventRegistration> findByEventId(Long eventId);

    @Query("SELECT * FROM event_registrations ORDER BY id ASC LIMIT :limit OFFSET :offset")
    Flux<EventRegistration> findAllPaged(int limit, long offset);

    @Query("""
            SELECT * FROM event_registrations
            WHERE (:eventId IS NULL OR event_id = :eventId)
              AND (:userId IS NULL OR user_id = :userId)
            ORDER BY id ASC
            LIMIT :limit OFFSET :offset
            """)
    Flux<EventRegistration> findFilteredPaged(Long eventId, Long userId, int limit, long offset);

    @Query("""
            SELECT COUNT(*) FROM event_registrations
            WHERE (:eventId IS NULL OR event_id = :eventId)
              AND (:userId IS NULL OR user_id = :userId)
            """)
    Mono<Long> countFiltered(Long eventId, Long userId);
}
