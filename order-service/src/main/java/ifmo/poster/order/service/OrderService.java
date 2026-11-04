package ifmo.poster.order.service;

import ifmo.poster.contracts.enums.EventStatus;
import ifmo.poster.contracts.enums.OrderStatus;
import ifmo.poster.contracts.event.ReserveTicketsRequest;
import ifmo.poster.contracts.event.TicketReservationDto;
import ifmo.poster.contracts.exception.ConflictException;
import ifmo.poster.order.dto.CreateOrderItemRequest;
import ifmo.poster.order.dto.CreateOrderRequest;
import ifmo.poster.order.dto.OrderResponse;
import ifmo.poster.order.dto.UpdateOrderStatusRequest;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final UserClientService userClientService;
    private final EventClientService eventClientService;
    private final OrderPersistenceService persistenceService;

    public OrderService(
            UserClientService userClientService,
            EventClientService eventClientService,
            OrderPersistenceService persistenceService
    ) {
        this.userClientService = userClientService;
        this.eventClientService = eventClientService;
        this.persistenceService = persistenceService;
    }

    public Mono<OrderResponse> create(CreateOrderRequest request) {
        return userClientService.getById(request.userId())
                .then(reserveAll(request.items()))
                .flatMap(reservations -> blocking(() -> persistenceService.createPendingOrder(request, reservations))
                        .onErrorResume(error -> compensate(reservations).then(Mono.error(error))));
    }

    public Mono<OrderResponse> getById(Long id) {
        return blocking(() -> persistenceService.getById(id));
    }

    public Mono<Page<OrderResponse>> findAll(Long userId, int page, int size) {
        return blocking(() -> persistenceService.findAll(userId, page, size));
    }

    public Mono<OrderResponse> updateStatus(Long id, UpdateOrderStatusRequest request) {
        if (request.status() == OrderStatus.CONFIRMED) {
            return blocking(() -> persistenceService.confirm(id));
        }
        if (request.status() == OrderStatus.CANCELLED) {
            return cancel(id);
        }
        return Mono.error(new ConflictException(
                "Поддерживаются только статусы CONFIRMED и CANCELLED"
        ));
    }

    public Mono<OrderResponse> cancel(Long id) {
        return blocking(() -> persistenceService.reservationIds(id))
                .flatMap(reservationIds -> Flux.fromIterable(reservationIds)
                        .concatMap(reservationId -> eventClientService.cancelReservation(reservationId)
                                .onErrorResume(error -> {
                                    log.warn("Не удалось отменить резерв {}: {}", reservationId, error.toString());
                                    return Mono.empty();
                                })
                        )
                        .then(blocking(() -> persistenceService.markCancelled(id))));
    }

    private Mono<List<TicketReservationDto>> reserveAll(List<CreateOrderItemRequest> items) {
        List<TicketReservationDto> reserved = new ArrayList<>();
        return Flux.fromIterable(items)
                .concatMap(item -> validateAndReserve(item)
                        .doOnNext(reserved::add)
                        .onErrorResume(error -> compensate(reserved).then(Mono.error(error))))
                .collectList();
    }

    private Mono<TicketReservationDto> validateAndReserve(CreateOrderItemRequest item) {
        return eventClientService.getById(item.eventId())
                .flatMap(event -> {
                    if (event.status() != EventStatus.ACTIVE) {
                        return Mono.error(new ConflictException(
                                "Событие недоступно для заказа: id=" + item.eventId()
                        ));
                    }
                    return eventClientService.reserve(new ReserveTicketsRequest(
                            item.eventId(),
                            item.ticketTypeId(),
                            item.quantity()
                    ));
                });
    }

    private Mono<Void> compensate(List<TicketReservationDto> reservations) {
        return Flux.fromIterable(reservations)
                .concatMap(reservation -> eventClientService.cancelReservation(reservation.reservationId())
                        .onErrorResume(error -> {
                            log.warn(
                                    "Компенсация резерва {} не удалась: {}",
                                    reservation.reservationId(),
                                    error.toString()
                            );
                            return Mono.empty();
                        }))
                .then();
    }

    private <T> Mono<T> blocking(java.util.concurrent.Callable<T> action) {
        return Mono.fromCallable(action).subscribeOn(Schedulers.boundedElastic());
    }
}
