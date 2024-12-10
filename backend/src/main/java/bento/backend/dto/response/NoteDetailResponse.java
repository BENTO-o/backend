package bento.backend.dto.response;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import java.util.List;
import java.util.Map;


@Getter
@Builder
public class NoteDetailResponse {
	private Long noteId;
	private String title;
	private String folder;
	private String createdAt;
	private String duration;
	private JsonNode content;
	private List<String> topics;
	private List<Map<String, Object>> bookmarks;
	private List<Map<String, Object>> memos;
	// TODO : AI 응답 형식 보고 수정 예정
	private List<String> AI;
}
