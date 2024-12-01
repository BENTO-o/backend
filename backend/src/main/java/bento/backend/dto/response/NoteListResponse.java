package bento.backend.dto.response;

import bento.backend.constant.SuccessMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
public class NoteListResponse {
	private Long noteId;
	private String title;
	private String folder;
	private String createdAt;
	private String duration;

}
