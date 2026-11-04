package ifmo.poster.event.converter;

import ifmo.poster.contracts.enums.WaitlistStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class WaitlistStatusReadConverter implements Converter<String, WaitlistStatus> {
    @Override
    public WaitlistStatus convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            return WaitlistStatus.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Недопустимый статус листа ожидания: " + source, e);
        }
    }
}