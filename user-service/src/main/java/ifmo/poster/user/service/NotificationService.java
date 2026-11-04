package ifmo.poster.user.service;

import ifmo.poster.user.dto.CreateNotificationRequest;
import ifmo.poster.user.dto.NotificationResponse;
import ifmo.poster.user.dto.UpdateNotificationRequest;
import ifmo.poster.user.entity.Notification;
import ifmo.poster.user.entity.User;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.user.mapper.NotificationMapper;
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
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public Mono<NotificationResponse> create(CreateNotificationRequest request) {
        return Mono.fromCallable(() -> transactionTemplate.execute(status -> {
                    User user = requireUser(request.userId());
                    Notification saved = notificationRepository.save(
                            NotificationMapper.toEntity(request, user)
                    );
                    return NotificationMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<NotificationResponse> getById(Long id) {
        return Mono.fromCallable(() -> NotificationMapper.toResponse(requireNotification(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<NotificationResponse>> findAll(Long userId, int page, int size) {
        return Mono.fromCallable(() -> {
                    Pagination.validate(page, size);
                    Pageable pageable = PageRequest.of(page, size);
                    if (userId != null) {
                        requireUser(userId);
                        return notificationRepository
                                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                                .map(NotificationMapper::toResponse);
                    }
                    return notificationRepository.findAll(pageable).map(NotificationMapper::toResponse);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<NotificationResponse> update(Long id, UpdateNotificationRequest request) {
        return Mono.fromCallable(() -> transactionTemplate.execute(status -> {
                    Notification notification = requireNotification(id);
                    NotificationMapper.updateEntity(notification, request);
                    return NotificationMapper.toResponse(notificationRepository.save(notification));
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> delete(Long id) {
        return Mono.<Void>fromRunnable(() -> transactionTemplate.executeWithoutResult(status -> {
                    requireNotification(id);
                    notificationRepository.deleteById(id);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: id=" + userId));
    }

    private Notification requireNotification(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Уведомление не найдено: id=" + id));
    }
}
