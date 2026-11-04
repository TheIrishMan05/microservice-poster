package ifmo.poster.event.service;

import ifmo.poster.event.dto.CreateTagRequest;
import ifmo.poster.event.dto.TagResponse;
import ifmo.poster.event.dto.UpdateTagRequest;
import ifmo.poster.event.entity.Tag;
import ifmo.poster.contracts.exception.ConflictException;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.event.mapper.TagMapper;
import ifmo.poster.event.repository.TagRepository;
import ifmo.poster.contracts.support.Pagination;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Mono<TagResponse> create(CreateTagRequest request) {
        return tagRepository.existsByNameIgnoreCase(request.name().trim())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new ConflictException("Тег уже существует: " + request.name()));
                    }
                    return tagRepository.save(TagMapper.toEntity(request)).map(TagMapper::toResponse);
                });
    }

    public Mono<TagResponse> getById(Long id) {
        return requireTag(id).map(TagMapper::toResponse);
    }

    public Mono<List<TagResponse>> findAll(int page, int size) {
        long offset = Pagination.offset(page, size);
        return tagRepository.findAllPaged(size, offset)
                .map(TagMapper::toResponse)
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
                    return tagRepository.save(tag);
                })
                .map(TagMapper::toResponse);
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
                    return tagRepository.save(tag);
                }));
    }
}
