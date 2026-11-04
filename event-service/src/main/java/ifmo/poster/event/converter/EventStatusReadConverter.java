package ifmo.poster.event.converter;

import ifmo.poster.contracts.enums.EventStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class EventStatusReadConverter implements Converter<String, EventStatus> {

    public EventStatus convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        } 
        try {
            return EventStatus.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Недопустимый статус события: " + source, e);
        }
    }
    
}
