package ifmo.poster.event.repository;

import ifmo.poster.event.entity.EventRegistration;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventRegistrationRepository extends R2dbcRepository<EventRegistration, Long> {
    Mono<Boolean> existsByUserIdAndEventId(Long userId, Long eventId);
    Mono<EventRegistration> findByUserIdAndEventId(Long userId, Long eventId);
    Flux<EventRegistration> findByUserId(Long userId);
    Flux<EventRegistration> findByEventId(Long eventId);
}