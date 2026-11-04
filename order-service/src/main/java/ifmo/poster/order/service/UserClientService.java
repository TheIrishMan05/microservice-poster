package ifmo.poster.order.service;

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
        return Mono.error(new ServiceUnavailableException(
                "Сервис пользователей временно недоступен. userId=" + userId,
                cause
        ));
    }
}
