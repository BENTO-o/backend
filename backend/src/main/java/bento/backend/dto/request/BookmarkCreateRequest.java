package bento.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookmarkCreateRequest {
    @NotNull(message = "Note ID is mandatory")
    private Long noteId;

    @NotBlank(message = "Timestamp is mandatory")
    @Pattern(
            regexp = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$",
            message = "Timestamp must follow the format HH:mm:ss (e.g., 14:30:15)"
    )
    private String timestamp;
}
