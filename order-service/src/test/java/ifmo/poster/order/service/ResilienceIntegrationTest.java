package ifmo.poster.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import feign.FeignException;
import feign.Request;
import feign.Response;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import ifmo.poster.order.client.UserClient;
import ifmo.poster.order.exception.ServiceUnavailableException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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
                "resilience4j.retry.instances.userService.waitDuration=1ms",
                "resilience4j.retry.instances.userService.ignoreExceptions=ifmo.poster.contracts.exception.ResourceNotFoundException",
                "resilience4j.circuitbreaker.instances.userService.ignoreExceptions=ifmo.poster.contracts.exception.ResourceNotFoundException"
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

    @Test
    void convertsFeign404ToResourceNotFound() {
        calls.set(0);
        StepVerifier.create(service.getById(0L))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResourceNotFoundException.class);
                    assertThat(error.getMessage()).contains("id=0");
                })
                .verify();

        assertThat(calls.get()).isEqualTo(1);
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
                if (userId != null && userId == 0L) {
                    Request request = Request.create(
                            Request.HttpMethod.GET,
                            "/internal/users/0",
                            Collections.emptyMap(),
                            null,
                            StandardCharsets.UTF_8,
                            null
                    );
                    throw FeignException.errorStatus(
                            "getById",
                            Response.builder()
                                    .status(404)
                                    .reason("Not Found")
                                    .request(request)
                                    .headers(Collections.emptyMap())
                                    .build()
                    );
                }
                throw new IllegalStateException("user-service is down");
            };
        }
    }
}
