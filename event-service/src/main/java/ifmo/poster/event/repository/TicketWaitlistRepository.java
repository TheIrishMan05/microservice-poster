package ifmo.poster.event.repository;

import ifmo.poster.event.entity.TicketWaitlist;
import ifmo.poster.contracts.enums.WaitlistStatus;
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
}