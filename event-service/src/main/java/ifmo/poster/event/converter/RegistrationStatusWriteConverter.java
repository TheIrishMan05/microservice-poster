package ifmo.poster.event.converter;

import ifmo.poster.contracts.enums.RegistrationStatus; 
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class RegistrationStatusWriteConverter implements Converter<RegistrationStatus, String> {
    @Override
    public String convert(RegistrationStatus source) {
        if (source == null || source.name() == null || source.name().isEmpty()) {
            return null;
        }
        return source.name();
    }
}
