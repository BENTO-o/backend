package bento.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NoteStatusResponse {
    private final String status;
}
