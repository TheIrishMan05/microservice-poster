package ifmo.poster.event.repository;

import ifmo.poster.event.entity.TicketType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TicketTypeRepository extends R2dbcRepository<TicketType, Long> {

    Mono<Boolean> existsByTypeNameIgnoreCase(String typeName);

    Mono<TicketType> findByTypeNameIgnoreCase(String typeName);

    @Query("SELECT * FROM ticket_types ORDER BY id ASC LIMIT :limit OFFSET :offset")
    Flux<TicketType> findAllPaged(int limit, long offset);
}
