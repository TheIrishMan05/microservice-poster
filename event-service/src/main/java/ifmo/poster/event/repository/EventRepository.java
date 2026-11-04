package ifmo.poster.event.repository;

import ifmo.poster.event.entity.Event;
import ifmo.poster.contracts.enums.EventStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventRepository extends R2dbcRepository<Event, Long> {

    Flux<Event> findByStatus(EventStatus status);

    Mono<Event> findByIdAndStatus(Long id, EventStatus status);

    @Query("""
            SELECT * FROM events
            WHERE (:status IS NULL OR status = :status)
              AND id > :lastSeenId
            ORDER BY id ASC
            LIMIT :limit
            """)
    Flux<Event> findNextBatchOfEvents(String status, Long lastSeenId, int limit);
}
