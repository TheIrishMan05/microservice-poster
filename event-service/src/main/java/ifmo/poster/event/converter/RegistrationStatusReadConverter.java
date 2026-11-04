package ifmo.poster.event.converter;

import ifmo.poster.contracts.enums.RegistrationStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class RegistrationStatusReadConverter implements Converter<String, RegistrationStatus> {
    @Override
    public RegistrationStatus convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            return RegistrationStatus.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Недопустимый статус регистрации: " + source, e);
        }
    }
}