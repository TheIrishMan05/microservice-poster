package ifmo.poster.event.service;

import ifmo.poster.contracts.enums.EventStatus;
import ifmo.poster.contracts.event.EventBriefDto;
import ifmo.poster.event.dto.CreateEventRequest;
import ifmo.poster.event.dto.EventResponse;
import ifmo.poster.event.dto.TagResponse;
import ifmo.poster.event.dto.UpdateEventRequest;
import ifmo.poster.event.entity.Event;
import ifmo.poster.event.entity.EventTag;
import ifmo.poster.event.entity.Seat;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.event.mapper.EventMapper;
import ifmo.poster.event.mapper.TagMapper;
import ifmo.poster.event.repository.EventRepository;
import ifmo.poster.event.repository.EventTagRepository;
import ifmo.poster.event.repository.SeatRepository;
import ifmo.poster.event.repository.TagRepository;
import ifmo.poster.event.repository.TicketTypeRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import ifmo.poster.contracts.support.Pagination;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ifmo.poster.event.config.EventProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventTagRepository eventTagRepository;
    private final TagRepository tagRepository;
    private final SeatRepository seatRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TagService tagService;
    private final TransactionalOperator transactionalOperator;
    private final DatabaseClient databaseClient;
    private final Validator validator;
    private final EventProperties eventProperties;

    public Mono<EventResponse> create(CreateEventRequest request) {
        Event event = EventMapper.toEntity(request);
        validateOrThrow(event);
        Mono<EventResponse> pipeline = eventRepository.save(event)
                .flatMap(saved -> replaceTags(saved.getId(), request.tagNames()).thenReturn(saved))
                .flatMap(saved -> createDefaultSeats(saved.getId()).thenReturn(saved))
                .flatMap(this::toResponse);
        return transactionalOperator.transactional(pipeline);
    }

    public Mono<List<EventResponse>> createBulk(List<CreateEventRequest> requests) {
        return Flux.fromIterable(requests)
                .concatMap(this::create)
                .collectList();
    }

    public Mono<Void> clearAllData() {
        return databaseClient.sql("""
                        TRUNCATE TABLE
                            ticket_reservation_holds,
                            ticket_reservations,
                            ticket_waitlists,
                            event_registrations,
                            seats,
                            event_tags,
                            events,
                            tags,
                            ticket_types
                        RESTART IDENTITY CASCADE
                        """)
                .then();
    }

    public Mono<EventResponse> getById(Long id) {
        return requireEvent(id).flatMap(this::toResponse);
    }

    public Mono<EventBriefDto> getBriefById(Long id) {
        return requireEvent(id)
                .flatMap(event -> Mono.just(EventMapper.toBrief(event)));
    }

    public Mono<List<EventResponse>> findNextBatch(Long afterId, int limit, EventStatus status) {
        Pagination.validateLimit(limit);
        long cursor = afterId == null ? 0L : afterId;
        String statusFilter = status == null ? null : status.name();
        return eventRepository.findNextBatchOfEvents(statusFilter, cursor, limit)
                .concatMap(this::toResponse)
                .collectList();
    }

    public Mono<EventResponse> update(Long id, UpdateEventRequest request) {
        Mono<EventResponse> pipeline = requireEvent(id)
                .flatMap(event -> {
                    EventMapper.updateEntity(event, request);
                    validateOrThrow(event);
                    return eventRepository.save(event);
                })
                .flatMap(saved -> replaceTags(saved.getId(), request.tagNames()).thenReturn(saved))
                .flatMap(this::toResponse);
        return transactionalOperator.transactional(pipeline);
    }

    public Mono<Void> delete(Long id) {
        Mono<Void> pipeline = requireEvent(id)
                .flatMap(event -> eventTagRepository.deleteByEventId(event.getId()).thenReturn(event))
                .flatMap(event -> eventRepository.deleteById(event.getId()));
        return transactionalOperator.transactional(pipeline);
    }

    Mono<Event> requireEvent(Long id) {
        return eventRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Событие не найдено: id=" + id)));
    }

    private Mono<Void> replaceTags(Long eventId, List<String> tagNames) {
        List<String> names = tagNames == null ? List.of() : tagNames;
        return eventTagRepository.deleteByEventId(eventId)
                .thenMany(Flux.fromIterable(names)
                        .map(String::trim)
                        .filter(name -> !name.isBlank())
                        .distinct(String::toLowerCase)
                        .concatMap(tagService::findOrCreateByName)
                        .concatMap(tag -> eventTagRepository.save(new EventTag(eventId, tag.getId()))))
                .then();
    }

    private Mono<Void> createDefaultSeats(Long eventId) {
        int seatsPerType = eventProperties.inventory().defaultSeatsPerTicketType();
        return ticketTypeRepository.findAll()
                .concatMap(ticketType -> Flux.range(1, seatsPerType)
                        .concatMap(number -> {
                            Seat seat = new Seat();
                            seat.setEventId(eventId);
                            seat.setTicketTypeId(ticketType.getId());
                            seat.setNumber(number);
                            seat.setRow("A");
                            seat.setSectorName(ticketType.getTypeName());
                            validateOrThrow(seat);
                            return seatRepository.save(seat);
                        }))
                .then();
    }

    private Mono<EventResponse> toResponse(Event event) {
        return eventTagRepository.findTagIdsByEventId(event.getId())
                .concatMap(tagRepository::findById)
                .flatMap(tag -> Mono.just(TagMapper.toResponse(tag)))
                .collectList()
                .flatMap(tags -> Mono.just(EventMapper.toResponse(event, tags)));
    }

    private void validateOrThrow(Object obj){
        Set<ConstraintViolation<Object>> violations = validator.validate(obj);
        if(!violations.isEmpty()){
            String message = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining("; "));
            throw new ConstraintViolationException(message, violations);
        }
    }
}
