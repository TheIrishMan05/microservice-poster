package ifmo.poster.event.service;

import ifmo.poster.contracts.enums.EventStatus;
import ifmo.poster.contracts.event.EventBriefDto;
import ifmo.poster.event.dto.CreateEventRequest;
import ifmo.poster.event.dto.EventResponse;
import ifmo.poster.event.dto.TagResponse;
import ifmo.poster.event.dto.UpdateEventRequest;
import ifmo.poster.event.entity.Event;
import ifmo.poster.event.entity.EventTag;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.event.mapper.EventMapper;
import ifmo.poster.event.mapper.TagMapper;
import ifmo.poster.event.repository.EventRepository;
import ifmo.poster.event.repository.EventTagRepository;
import ifmo.poster.event.repository.TagRepository;
import ifmo.poster.contracts.support.Pagination;
import java.util.List;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventTagRepository eventTagRepository;
    private final TagRepository tagRepository;
    private final TagService tagService;
    private final TransactionalOperator transactionalOperator;
    private final DatabaseClient databaseClient;

    public EventService(
            EventRepository eventRepository,
            EventTagRepository eventTagRepository,
            TagRepository tagRepository,
            TagService tagService,
            TransactionalOperator transactionalOperator,
            DatabaseClient databaseClient
    ) {
        this.eventRepository = eventRepository;
        this.eventTagRepository = eventTagRepository;
        this.tagRepository = tagRepository;
        this.tagService = tagService;
        this.transactionalOperator = transactionalOperator;
        this.databaseClient = databaseClient;
    }

    public Mono<EventResponse> create(CreateEventRequest request) {
        Mono<EventResponse> pipeline = eventRepository.save(EventMapper.toEntity(request))
                .flatMap(saved -> replaceTags(saved.getId(), request.tagNames()).thenReturn(saved))
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

    private Mono<EventResponse> toResponse(Event event) {
        return eventTagRepository.findTagIdsByEventId(event.getId())
                .concatMap(tagRepository::findById)
                .flatMap(tag -> Mono.just(TagMapper.toResponse(tag)))
                .collectList()
                .flatMap(tags -> Mono.just(EventMapper.toResponse(event, tags)));
    }
}
