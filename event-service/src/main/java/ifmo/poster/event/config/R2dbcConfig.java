package ifmo.poster.event.config;

import ifmo.poster.event.converter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class R2dbcConfig {
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        
        converters.add(new EventStatusReadConverter());
        converters.add(new EventStatusWriteConverter());
        
        converters.add(new RegistrationStatusReadConverter());
        converters.add(new RegistrationStatusWriteConverter());
        
        converters.add(new WaitlistStatusReadConverter());
        converters.add(new WaitlistStatusWriteConverter());
        
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
    }
}
