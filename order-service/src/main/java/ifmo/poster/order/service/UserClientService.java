package ifmo.poster.order.service;

import feign.FeignException;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.contracts.user.UserDto;
import ifmo.poster.order.client.RemoteCallErrors;
import ifmo.poster.order.client.UserClient;
import ifmo.poster.order.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class UserClientService {

    private final UserClient userClient;

    @Retry(name = "userService")
    @CircuitBreaker(name = "userService", fallbackMethod = "getByIdFallback")
    public Mono<UserDto> getById(Long userId) {
        return Mono.fromCallable(() -> fetchUser(userId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private UserDto fetchUser(Long userId) {
        try {
            return userClient.getById(userId);
        } catch (FeignException exception) {
            if (RemoteCallErrors.isNotFound(exception)) {
                throw new ResourceNotFoundException("Пользователь не найден: id=" + userId);
            }
            throw exception;
        }
    }

    private Mono<UserDto> getByIdFallback(Long userId, Throwable cause) {
        if (RemoteCallErrors.isNotFound(cause)) {
            return Mono.error(new ResourceNotFoundException("Пользователь не найден: id=" + userId));
        }
        return Mono.error(new ServiceUnavailableException(
                "Сервис пользователей временно недоступен. userId=" + userId,
                cause
        ));
    }
}
