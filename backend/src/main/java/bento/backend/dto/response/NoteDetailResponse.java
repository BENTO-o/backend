package bento.backend.dto.response;

import bento.backend.constant.SuccessMessages;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;


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
	private List<Map<String, String>> bookmarks;
	private List<Map<String, String>> memos;
	// TODO : AI 응답 형식 보고 수정 예정
	private List<String> AI;
}
