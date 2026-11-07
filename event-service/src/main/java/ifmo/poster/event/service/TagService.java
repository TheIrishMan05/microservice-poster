package ifmo.poster.event.service;

import ifmo.poster.contracts.exception.ConflictException;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.contracts.support.Pagination;
import ifmo.poster.event.dto.CreateTagRequest;
import ifmo.poster.event.dto.TagResponse;
import ifmo.poster.event.dto.UpdateTagRequest;
import ifmo.poster.event.entity.Tag;
import ifmo.poster.event.mapper.TagMapper;
import ifmo.poster.event.repository.TagRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final Validator validator;

    public TagService(TagRepository tagRepository, Validator validator) {
        this.tagRepository = tagRepository;
        this.validator = validator;
    }

    public Mono<TagResponse> create(CreateTagRequest request) {
        return tagRepository.existsByNameIgnoreCase(request.name().trim())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new ConflictException("Тег уже существует: " + request.name()));
                    }
                    
                    Tag entity = TagMapper.toEntity(request);
                    validateOrThrow(entity); // Валидация перед сохранением
                    
                    return tagRepository.save(entity)
                            .flatMap(saved -> Mono.just(TagMapper.toResponse(saved)));
                });
    }

    public Mono<TagResponse> getById(Long id) {
        return requireTag(id)
                .flatMap(tag -> Mono.just(TagMapper.toResponse(tag)));
    }

    public Mono<List<TagResponse>> findAll(int page, int size) {
        long offset = Pagination.offset(page, size);
        return tagRepository.findAllPaged(size, offset)
                .flatMap(tag -> Mono.just(TagMapper.toResponse(tag)))
                .collectList();
    }

    public Mono<TagResponse> update(Long id, UpdateTagRequest request) {
        return requireTag(id)
                .flatMap(tag -> tagRepository.findByNameIgnoreCase(request.name().trim())
                        .flatMap(existing -> {
                            if (!existing.getId().equals(id)) {
                                return Mono.<Tag>error(new ConflictException("Тег уже существует: " + request.name()));
                            }
                            return Mono.just(tag);
                        })
                        .switchIfEmpty(Mono.just(tag)))
                .flatMap(tag -> {
                    TagMapper.updateEntity(tag, request);
                    validateOrThrow(tag); 
                    
                    return tagRepository.save(tag);
                })
                .flatMap(saved -> Mono.just(TagMapper.toResponse(saved)));
    }

    public Mono<Void> delete(Long id) {
        return requireTag(id).flatMap(tag -> tagRepository.deleteById(id));
    }

    Mono<Tag> requireTag(Long id) {
        return tagRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Тег не найден: id=" + id)));
    }

    Mono<Tag> findOrCreateByName(String name) {
        String normalized = name.trim();
        return tagRepository.findByNameIgnoreCase(normalized)
                .switchIfEmpty(Mono.defer(() -> {
                    Tag tag = new Tag();
                    tag.setName(normalized);
                    validateOrThrow(tag); 
                    
                    return tagRepository.save(tag);
                }));
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