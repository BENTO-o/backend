package bento.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemoResponse {
    private Long memoId;
    private Long noteId;
    private String text;
    private String timestamp;
}
