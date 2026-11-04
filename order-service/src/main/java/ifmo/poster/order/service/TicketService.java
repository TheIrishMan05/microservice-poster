package ifmo.poster.order.service;

import ifmo.poster.contracts.enums.TicketStatus;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.contracts.support.Pagination;
import ifmo.poster.order.dto.TicketResponse;
import ifmo.poster.order.mapper.OrderMapper;
import ifmo.poster.order.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Mono<TicketResponse> getById(Long id) {
        return Mono.fromCallable(() -> ticketRepository.findById(id)
                        .map(OrderMapper::toTicketResponse)
                        .orElseThrow(() -> new ResourceNotFoundException("Билет не найден: id=" + id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<TicketResponse>> findAll(Long eventId, TicketStatus status, int page, int size) {
        return Mono.fromCallable(() -> {
                    Pagination.validate(page, size);
                    PageRequest pageable = PageRequest.of(page, size);
                    if (eventId != null && status != null) {
                        return ticketRepository.findByEventIdAndStatus(eventId, status, pageable)
                                .map(OrderMapper::toTicketResponse);
                    }
                    if (eventId != null) {
                        return ticketRepository.findByEventId(eventId, pageable)
                                .map(OrderMapper::toTicketResponse);
                    }
                    return ticketRepository.findAll(pageable).map(OrderMapper::toTicketResponse);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
