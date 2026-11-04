package ifmo.poster.event.converter;

import ifmo.poster.contracts.enums.EventStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class EventStatusWriteConverter implements Converter<EventStatus, String> {
    @Override
    public String convert(EventStatus source) {
        if (source == null || source.name() == null || source.name().isEmpty()) {
            return null;
        }
        return source.name();
    }
}
