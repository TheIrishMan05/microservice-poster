package ifmo.poster.user.service;

import ifmo.poster.contracts.user.UserDto;
import ifmo.poster.user.dto.CreateUserRequest;
import ifmo.poster.user.dto.UpdateUserRequest;
import ifmo.poster.user.dto.UserResponse;
import ifmo.poster.user.entity.User;
import ifmo.poster.contracts.exception.ConflictException;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.user.mapper.UserMapper;
import ifmo.poster.user.repository.NotificationRepository;
import ifmo.poster.user.repository.UserRepository;
import ifmo.poster.contracts.support.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final TransactionTemplate transactionTemplate;

    public UserService(
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public Mono<UserResponse> create(CreateUserRequest request) {
        return Mono.fromCallable(() -> transactionTemplate.execute(status -> {
                    assertUnique(request.userName(), request.email(), null);
                    User saved = userRepository.save(UserMapper.toEntity(request));
                    return UserMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<UserResponse> getById(Long id) {
        return Mono.fromCallable(() -> UserMapper.toResponse(requireUser(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<UserDto> getContractById(Long id) {
        return Mono.fromCallable(() -> UserMapper.toContract(requireUser(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<UserResponse>> findAll(int page, int size) {
        return Mono.fromCallable(() -> {
                    Pagination.validate(page, size);
                    Pageable pageable = PageRequest.of(page, size);
                    return userRepository.findAll(pageable).map(UserMapper::toResponse);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<UserResponse> update(Long id, UpdateUserRequest request) {
        return Mono.fromCallable(() -> transactionTemplate.execute(status -> {
                    User user = requireUser(id);
                    assertUnique(request.userName(), request.email(), id);
                    UserMapper.updateEntity(user, request);
                    return UserMapper.toResponse(userRepository.save(user));
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> delete(Long id) {
        return Mono.<Void>fromRunnable(() -> transactionTemplate.executeWithoutResult(status -> {
                    requireUser(id);
                    notificationRepository.deleteByUserId(id);
                    userRepository.deleteById(id);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: id=" + id));
    }

    private void assertUnique(String userName, String email, Long currentUserId) {
        userRepository.findByUserName(userName)
                .filter(existing -> currentUserId == null || !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new ConflictException("Имя пользователя уже занято: " + userName);
                });

        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email.trim())
                .filter(existing -> currentUserId == null || !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new ConflictException("Email уже занят: " + email);
                });
    }
}
