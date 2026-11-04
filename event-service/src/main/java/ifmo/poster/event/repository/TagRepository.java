package ifmo.poster.event.repository;

import ifmo.poster.event.entity.Tag;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TagRepository extends R2dbcRepository<Tag, Long> {

    Mono<Tag> findByNameIgnoreCase(String name);

    Mono<Boolean> existsByNameIgnoreCase(String name);

    @Query("SELECT * FROM tags ORDER BY id ASC LIMIT :limit OFFSET :offset")
    Flux<Tag> findAllPaged(int limit, long offset);
}
