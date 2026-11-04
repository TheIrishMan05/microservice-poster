package ifmo.poster.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.cloud.gateway.config.GatewayProperties;

class GatewayRoutesConfigurationTest {

    @Test
    void currentGatewayPrefixBindsExplicitRoute() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "spring.cloud.gateway.server.webflux.routes[0].id", "event-service",
                "spring.cloud.gateway.server.webflux.routes[0].uri", "lb://event-service",
                "spring.cloud.gateway.server.webflux.routes[0].predicates[0]", "Path=/api/events/**"
        ));

        GatewayProperties properties = new Binder(source)
                .bind("spring.cloud.gateway.server.webflux", Bindable.of(GatewayProperties.class))
                .get();

        assertThat(properties.getRoutes()).hasSize(1);
        assertThat(properties.getRoutes().getFirst().getId()).isEqualTo("event-service");
        assertThat(properties.getRoutes().getFirst().getUri().toString())
                .isEqualTo("lb://event-service");
    }
}
