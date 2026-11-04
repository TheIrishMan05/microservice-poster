package ifmo.poster.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import ifmo.poster.order.client.UserClient;
import ifmo.poster.order.exception.ServiceUnavailableException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

@SpringBootTest(
        classes = ResilienceIntegrationTest.TestApplication.class,
        properties = {
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "resilience4j.retry.instances.userService.maxAttempts=2",
                "resilience4j.retry.instances.userService.waitDuration=1ms"
        }
)
class ResilienceIntegrationTest {

    @Autowired
    private UserClientService service;

    @Autowired
    private AtomicInteger calls;

    @Test
    void convertsFeignFailureToServiceUnavailableError() {
        StepVerifier.create(service.getById(42L))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ServiceUnavailableException.class);
                    assertThat(error.getMessage())
                            .contains("Сервис пользователей временно недоступен")
                            .contains("userId=42");
                })
                .verify();

        assertThat(calls.get()).isPositive();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            LiquibaseAutoConfiguration.class
    })
    @Import(UserClientService.class)
    static class TestApplication {

        @Bean
        AtomicInteger calls() {
            return new AtomicInteger();
        }

        @Bean
        UserClient userClient(AtomicInteger calls) {
            return userId -> {
                calls.incrementAndGet();
                throw new IllegalStateException("user-service is down");
            };
        }
    }
}
