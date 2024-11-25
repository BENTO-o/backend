package bento.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemoCreateRequest {
    @NotNull(message = "Note ID is mandatory")
    private Long noteId;

    @NotNull(message = "Timestamp is mandatory")
    @Pattern(
            regexp = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$",
            message = "Timestamp must follow the format HH:mm:ss (e.g., 14:30:15)"
    )
    private String timestamp;

    @NotNull(message = "Text is mandatory")
    private String text;
}
