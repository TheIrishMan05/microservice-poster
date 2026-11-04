package ifmo.poster.event.repository;

import ifmo.poster.event.entity.EventTag;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventTagRepository extends R2dbcRepository<EventTag, Long> {

    @Query("SELECT tag_id FROM event_tags WHERE event_id = :eventId")
    Flux<Long> findTagIdsByEventId(Long eventId);

    @Modifying
    @Query("DELETE FROM event_tags WHERE event_id = :eventId")
    Mono<Void> deleteByEventId(Long eventId);

    Mono<Boolean> existsByEventIdAndTagId(Long eventId, Long tagId);
}
