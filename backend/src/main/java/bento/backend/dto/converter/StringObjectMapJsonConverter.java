package bento.backend.dto.converter;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public class StringObjectMapJsonConverter extends GenericJsonConverter<Map<String, Object>> {
    public StringObjectMapJsonConverter() {
        super(new TypeReference<>() {});
    }
}
