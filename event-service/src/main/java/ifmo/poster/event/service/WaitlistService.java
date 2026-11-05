package ifmo.poster.event.service;

import ifmo.poster.contracts.enums.WaitlistStatus;
import ifmo.poster.event.dto.CreateWaitlistRequest;
import ifmo.poster.event.dto.UpdateWaitlistRequest;
import ifmo.poster.event.dto.WaitlistResponse;
import ifmo.poster.event.entity.TicketWaitlist;
import ifmo.poster.contracts.exception.ConflictException;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.event.mapper.WaitlistMapper;
import ifmo.poster.event.repository.TicketWaitlistRepository;
import ifmo.poster.contracts.support.Pagination;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public class WaitlistService {

    private final TicketWaitlistRepository waitlistRepository;
    private final EventService eventService;
    private final TicketTypeService ticketTypeService;
    private final TransactionalOperator transactionalOperator;

    public WaitlistService(
            TicketWaitlistRepository waitlistRepository,
            EventService eventService,
            TicketTypeService ticketTypeService,
            TransactionalOperator transactionalOperator
    ) {
        this.waitlistRepository = waitlistRepository;
        this.eventService = eventService;
        this.ticketTypeService = ticketTypeService;
        this.transactionalOperator = transactionalOperator;
    }

    public Mono<WaitlistResponse> create(CreateWaitlistRequest request) {
        Mono<WaitlistResponse> pipeline = eventService.requireEvent(request.eventId())
                .then(ticketTypeService.requireTicketType(request.ticketTypeId()))
                .then(waitlistRepository.findByUserIdAndEventIdAndTicketTypeIdAndStatus(
                        request.userId(),
                        request.eventId(),
                        request.ticketTypeId(),
                        WaitlistStatus.WAITING
                ))
                .flatMap(existing -> Mono.<TicketWaitlist>error(new ConflictException(
                        "Активная запись waitlist уже существует"
                )))
                .switchIfEmpty(Mono.defer(() -> waitlistRepository.save(WaitlistMapper.toEntity(request))))
                .flatMap(saved -> Mono.just(WaitlistMapper.toResponse(saved)));
        return transactionalOperator.transactional(pipeline);
    }

    public Mono<WaitlistResponse> getById(Long id) {
        return requireWaitlist(id)
                .flatMap(waitlist -> Mono.just(WaitlistMapper.toResponse(waitlist)));
    }

    public Mono<Tuple2<List<WaitlistResponse>, Long>> findAll(Long eventId, Long userId, int page, int size) {
        long offset = Pagination.offset(page, size);
        Mono<List<WaitlistResponse>> items = waitlistRepository
                .findFilteredPaged(eventId, userId, size, offset)
                .flatMap(waitlist -> Mono.just(WaitlistMapper.toResponse(waitlist)))
                .collectList();
        Mono<Long> total = waitlistRepository.countFiltered(eventId, userId);
        return Mono.zip(items, total);
    }

    public Mono<WaitlistResponse> update(Long id, UpdateWaitlistRequest request) {
        return requireWaitlist(id)
                .flatMap(waitlist -> {
                    WaitlistMapper.updateEntity(waitlist, request);
                    return waitlistRepository.save(waitlist);
                })
                .flatMap(saved -> Mono.just(WaitlistMapper.toResponse(saved)));
    }

    public Mono<Void> delete(Long id) {
        return requireWaitlist(id).flatMap(waitlist -> waitlistRepository.deleteById(id));
    }

    private Mono<TicketWaitlist> requireWaitlist(Long id) {
        return waitlistRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Запись waitlist не найдена: id=" + id)));
    }
}
