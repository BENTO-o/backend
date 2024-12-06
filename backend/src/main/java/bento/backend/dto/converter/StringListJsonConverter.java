package bento.backend.dto.converter;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class StringListJsonConverter extends GenericJsonConverter<List<String>> {
    public StringListJsonConverter() {
        super(new TypeReference<>() {});
    }
}
