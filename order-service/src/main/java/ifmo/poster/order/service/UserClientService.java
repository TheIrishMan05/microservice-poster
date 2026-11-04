package ifmo.poster.order.service;

import feign.FeignException;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.contracts.user.UserDto;
import ifmo.poster.order.client.UserClient;
import ifmo.poster.order.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class UserClientService {

    private final UserClient userClient;

    public UserClientService(UserClient userClient) {
        this.userClient = userClient;
    }

    @Retry(name = "userService", fallbackMethod = "getByIdFallback")
    @CircuitBreaker(name = "userService", fallbackMethod = "getByIdFallback")
    public Mono<UserDto> getById(Long userId) {
        return Mono.fromCallable(() -> userClient.getById(userId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<UserDto> getByIdFallback(Long userId, Throwable cause) {
        if (isNotFound(cause)) {
            return Mono.error(new ResourceNotFoundException("Пользователь не найден: id=" + userId));
        }
        return Mono.error(new ServiceUnavailableException(
                "Сервис пользователей временно недоступен. userId=" + userId,
                cause
        ));
    }

    private static boolean isNotFound(Throwable cause) {
        Throwable current = cause;
        while (current != null) {
            if (current instanceof FeignException.NotFound) {
                return true;
            }
            if (current instanceof FeignException feignException && feignException.status() == 404) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
