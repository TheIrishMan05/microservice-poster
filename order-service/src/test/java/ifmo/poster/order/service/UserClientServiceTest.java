package ifmo.poster.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ifmo.poster.contracts.enums.Role;
import ifmo.poster.contracts.user.UserDto;
import ifmo.poster.order.client.UserClient;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserClientServiceTest {

    @Mock
    private UserClient userClient;

    @Test
    void movesBlockingFeignCallToBoundedElasticScheduler() {
        AtomicReference<String> clientThread = new AtomicReference<>();
        UserDto expected = new UserDto(7L, "reader", "reader@example.com", Role.USER);
        when(userClient.getById(7L)).thenAnswer(invocation -> {
            clientThread.set(Thread.currentThread().getName());
            return expected;
        });

        StepVerifier.create(new UserClientService(userClient).getById(7L))
                .expectNext(expected)
                .verifyComplete();

        assertThat(clientThread.get()).contains("boundedElastic");
    }
}
