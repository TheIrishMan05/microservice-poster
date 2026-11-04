package ifmo.poster.event.converter;

import ifmo.poster.contracts.enums.WaitlistStatus; 
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class WaitlistStatusWriteConverter implements Converter<WaitlistStatus, String> {
    @Override
    public String convert(WaitlistStatus source) {
        if (source == null || source.name() == null || source.name().isEmpty()) {
            return null;
        }
        return source.name();
    }
}