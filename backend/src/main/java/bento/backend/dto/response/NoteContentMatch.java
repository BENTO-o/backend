package bento.backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteContentMatch {
    private String text;
    private String timestamp;
}

