package ifmo.poster.event.service;

import ifmo.poster.contracts.support.Pagination;
import ifmo.poster.event.dto.SeatResponse;
import ifmo.poster.event.mapper.SeatMapper;
import ifmo.poster.event.repository.SeatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final EventService eventService;

    public Mono<Tuple2<List<SeatResponse>, Long>> findByEvent(
            Long eventId,
            Long ticketTypeId,
            boolean availableOnly,
            int page,
            int size
    ) {
        Pagination.validate(page, size);
        long offset = Pagination.offset(page, size);

        return eventService.requireEvent(eventId).then(Mono.defer(() -> {
            Mono<List<SeatResponse>> items = (availableOnly
                    ? seatRepository.findAvailablePaged(eventId, ticketTypeId, size, offset)
                    : seatRepository.findPaged(eventId, ticketTypeId, size, offset))
                    .map(SeatMapper::toResponse)
                    .collectList();
            Mono<Long> total = availableOnly
                    ? seatRepository.countAvailable(eventId, ticketTypeId)
                    : seatRepository.countPaged(eventId, ticketTypeId);
            return Mono.zip(items, total);
        }));
    }
}
