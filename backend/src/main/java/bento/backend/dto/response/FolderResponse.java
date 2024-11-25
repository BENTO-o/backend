package bento.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FolderResponse {
	private Long folderId;
	private String folderName;
}
