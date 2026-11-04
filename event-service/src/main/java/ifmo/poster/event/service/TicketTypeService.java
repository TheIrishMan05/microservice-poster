package ifmo.poster.event.service;

import ifmo.poster.event.dto.CreateTicketTypeRequest;
import ifmo.poster.event.dto.TicketTypeResponse;
import ifmo.poster.event.dto.UpdateTicketTypeRequest;
import ifmo.poster.event.entity.TicketType;
import ifmo.poster.contracts.exception.ConflictException;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.event.mapper.TicketTypeMapper;
import ifmo.poster.event.repository.TicketTypeRepository;
import ifmo.poster.contracts.support.Pagination;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    public TicketTypeService(TicketTypeRepository ticketTypeRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
    }

    public Mono<TicketTypeResponse> create(CreateTicketTypeRequest request) {
        return ticketTypeRepository.existsByTypeNameIgnoreCase(request.typeName().trim())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new ConflictException(
                                "Тип билета уже существует: " + request.typeName()
                        ));
                    }
                    return ticketTypeRepository.save(TicketTypeMapper.toEntity(request))
                            .map(TicketTypeMapper::toResponse);
                });
    }

    public Mono<TicketTypeResponse> getById(Long id) {
        return requireTicketType(id).map(TicketTypeMapper::toResponse);
    }

    public Mono<List<TicketTypeResponse>> findAll(int page, int size) {
        long offset = Pagination.offset(page, size);
        return ticketTypeRepository.findAllPaged(size, offset)
                .map(TicketTypeMapper::toResponse)
                .collectList();
    }

    public Mono<TicketTypeResponse> update(Long id, UpdateTicketTypeRequest request) {
        return requireTicketType(id)
                .flatMap(ticketType -> ticketTypeRepository.findByTypeNameIgnoreCase(request.typeName().trim())
                        .flatMap(existing -> {
                            if (!existing.getId().equals(id)) {
                                return Mono.<TicketType>error(new ConflictException(
                                        "Тип билета уже существует: " + request.typeName()
                                ));
                            }
                            return Mono.just(ticketType);
                        })
                        .switchIfEmpty(Mono.just(ticketType)))
                .flatMap(ticketType -> {
                    TicketTypeMapper.updateEntity(ticketType, request);
                    return ticketTypeRepository.save(ticketType);
                })
                .map(TicketTypeMapper::toResponse);
    }

    public Mono<Void> delete(Long id) {
        return requireTicketType(id).flatMap(ticketType -> ticketTypeRepository.deleteById(id));
    }

    Mono<TicketType> requireTicketType(Long id) {
        return ticketTypeRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Тип билета не найден: id=" + id)));
    }
}
