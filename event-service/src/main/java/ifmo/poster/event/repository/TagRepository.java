package ifmo.poster.event.repository;

import ifmo.poster.event.entity.Tag;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface TagRepository extends R2dbcRepository<Tag, Long> {
    Mono<Tag> findByNameIgnoreCase(String name);
    Mono<Boolean> existsByNameIgnoreCase(String name);
}
