package bento.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkResponse {
    private Long bookmarkId;
    private Long noteId;
//    private String createdAt; // 노트 생성일이 아니라 북마크가 있어야 하는 위치값으로 변경
    private String timestamp;
}
