package bento.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NoteSearchResponse {
    private Long noteId;          // 노트 ID
    private String title;         // 노트 제목
    private String folder;        // 폴더 이름
    private String createdAt;     // 생성일
    private List<NoteContentMatch> matches; // 검색어가 포함된 문장 리스트
}
