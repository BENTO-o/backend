package bento.backend.dto.response;

import bento.backend.constant.SuccessMessages;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;


@Getter
@Builder
public class NoteDetailResponse {
	private Long noteId;
	private String title;
	private String folder;
	private String createdAt;
	private String duration;
	private JsonNode content;

	// TODO : AI 응답 형식 보고 수정 예정
	// private List<String> speakers;
	// private List<String> scripts;
}
