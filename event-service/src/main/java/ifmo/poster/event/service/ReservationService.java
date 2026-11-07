package ifmo.poster.event.service;

import ifmo.poster.contracts.enums.EventStatus;
import ifmo.poster.contracts.event.ReserveTicketsRequest;
import ifmo.poster.contracts.event.TicketReservationDto;
import ifmo.poster.contracts.exception.ConflictException;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.event.entity.TicketReservation;
import ifmo.poster.event.entity.TicketReservationHold;
import ifmo.poster.event.entity.TicketType;
import ifmo.poster.event.repository.SeatRepository;
import ifmo.poster.event.repository.TicketReservationHoldRepository;
import ifmo.poster.event.repository.TicketReservationRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final EventService eventService;
    private final TicketTypeService ticketTypeService;
    private final TicketReservationRepository reservationRepository;
    private final TicketReservationHoldRepository holdRepository;
    private final SeatRepository seatRepository;
    private final TransactionalOperator transactionalOperator;
    private final Validator validator; 

    public ReservationService(
            EventService eventService,
            TicketTypeService ticketTypeService,
            TicketReservationRepository reservationRepository,
            TicketReservationHoldRepository holdRepository,
            SeatRepository seatRepository,
            TransactionalOperator transactionalOperator,
            Validator validator
    ) {
        this.eventService = eventService;
        this.ticketTypeService = ticketTypeService;
        this.reservationRepository = reservationRepository;
        this.holdRepository = holdRepository;
        this.seatRepository = seatRepository;
        this.transactionalOperator = transactionalOperator;
        this.validator = validator;
    }

    public Mono<TicketReservationDto> reserve(ReserveTicketsRequest request) {
        if (request.quantity() < 1 || request.quantity() > 50) {
            return Mono.error(new IllegalArgumentException("quantity должен быть от 1 до 50"));
        }
        
        Mono<TicketReservationDto> pipeline = eventService.requireEvent(request.eventId())
                .flatMap(event -> {
                    if (event.getStatus() != EventStatus.ACTIVE) {
                        return Mono.error(new ConflictException(
                                "Резерв доступен только для ACTIVE событий"
                        ));
                    }
                    return ticketTypeService.requireTicketType(request.ticketTypeId())
                            .flatMap(ticketType -> createReservation(event.getId(), ticketType, request.quantity()));
                });
                
        return transactionalOperator.transactional(pipeline);
    }

    public Mono<Void> cancel(String reservationId) {
        Mono<Void> pipeline = reservationRepository.findByPublicId(reservationId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Резерв не найден: reservationId=" + reservationId
                )))
                .flatMap(reservation -> {
                    if ("CANCELLED".equals(reservation.getStatus())) {
                        return Mono.empty();
                    }
                    reservation.setStatus("CANCELLED");
                    validateOrThrow(reservation);
                    
                    return reservationRepository.save(reservation)
                            .thenMany(seatRepository.findByReservationId(reservationId))
                            .concatMap(seat -> {
                                seat.setReservationId(null);
                                return seatRepository.save(seat);
                            })
                            .then();
                });
        return transactionalOperator.transactional(pipeline);
    }

    private Mono<TicketReservationDto> createReservation(Long eventId, TicketType ticketType, int quantity) {
        String publicId = UUID.randomUUID().toString();
        TicketReservation reservation = new TicketReservation(
                publicId,
                eventId,
                ticketType.getId(),
                quantity,
                ticketType.getPrice()
        );
        
        validateOrThrow(reservation); 
        
        return reservationRepository.save(reservation)
                .flatMap(saved -> bindAvailableSeats(saved, publicId, quantity)
                        .then(createHolds(saved.getId(), quantity))
                        .flatMap(holdIds -> Mono.just(new TicketReservationDto(
                                publicId,
                                eventId,
                                ticketType.getId(),
                                quantity,
                                ticketType.getPrice(),
                                holdIds
                        ))));
    }

    private Mono<Void> bindAvailableSeats(TicketReservation reservation, String publicId, int quantity) {
        return seatRepository.findAvailableForUpdate(reservation.getEventId(), reservation.getTicketTypeId(), quantity)
                .collectList()
                .flatMap(seats -> {
                    if (seats.size() < quantity) {
                        return Mono.error(new ConflictException(
                                "Недостаточно свободных мест: нужно=" + quantity
                                        + ", доступно=" + seats.size()
                        ));
                    }
                    return Flux.fromIterable(seats)
                            .concatMap(seat -> {
                                seat.setReservationId(publicId);
                                return seatRepository.save(seat);
                            })
                            .then();
                });
    }

    private Mono<List<Long>> createHolds(Long reservationId, int quantity) {
        return Flux.range(0, quantity)
                .concatMap(ignored -> {
                    TicketReservationHold hold = new TicketReservationHold(reservationId);
                    validateOrThrow(hold); 
                    
                    return holdRepository.save(hold);
                })
                .map(TicketReservationHold::getId)
                .collectList();
    }

    private void validateOrThrow(Object obj) {
        Set<ConstraintViolation<Object>> violations = validator.validate(obj);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new ConstraintViolationException(message, violations);
        }
    }
}