package ifmo.poster.event.service;

import ifmo.poster.event.dto.CreateRegistrationRequest;
import ifmo.poster.event.dto.RegistrationResponse;
import ifmo.poster.event.dto.UpdateRegistrationRequest;
import ifmo.poster.event.entity.EventRegistration;
import ifmo.poster.contracts.exception.ConflictException;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.event.mapper.RegistrationMapper;
import ifmo.poster.event.repository.EventRegistrationRepository;
import ifmo.poster.contracts.support.Pagination;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public class RegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final EventService eventService;
    private final TransactionalOperator transactionalOperator;

    public RegistrationService(
            EventRegistrationRepository registrationRepository,
            EventService eventService,
            TransactionalOperator transactionalOperator
    ) {
        this.registrationRepository = registrationRepository;
        this.eventService = eventService;
        this.transactionalOperator = transactionalOperator;
    }

    public Mono<RegistrationResponse> create(CreateRegistrationRequest request) {
        Mono<RegistrationResponse> pipeline = eventService.requireEvent(request.eventId())
                .then(registrationRepository.existsByUserIdAndEventId(request.userId(), request.eventId()))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new ConflictException(
                                "Регистрация уже существует для userId=" + request.userId()
                                        + ", eventId=" + request.eventId()
                        ));
                    }
                    return registrationRepository.save(RegistrationMapper.toEntity(request));
                })
                .map(RegistrationMapper::toResponse);
        return transactionalOperator.transactional(pipeline);
    }

    public Mono<RegistrationResponse> getById(Long id) {
        return requireRegistration(id).map(RegistrationMapper::toResponse);
    }

    public Mono<Tuple2<List<RegistrationResponse>, Long>> findAll(Long eventId, Long userId, int page, int size) {
        long offset = Pagination.offset(page, size);
        Mono<List<RegistrationResponse>> items = registrationRepository
                .findFilteredPaged(eventId, userId, size, offset)
                .map(RegistrationMapper::toResponse)
                .collectList();
        Mono<Long> total = registrationRepository.countFiltered(eventId, userId);
        return Mono.zip(items, total);
    }

    public Mono<RegistrationResponse> update(Long id, UpdateRegistrationRequest request) {
        return requireRegistration(id)
                .flatMap(registration -> {
                    RegistrationMapper.updateEntity(registration, request);
                    return registrationRepository.save(registration);
                })
                .map(RegistrationMapper::toResponse);
    }

    public Mono<Void> delete(Long id) {
        return requireRegistration(id).flatMap(registration -> registrationRepository.deleteById(id));
    }

    private Mono<EventRegistration> requireRegistration(Long id) {
        return registrationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Регистрация не найдена: id=" + id)));
    }
}
