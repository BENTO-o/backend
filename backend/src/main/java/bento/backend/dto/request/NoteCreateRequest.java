package bento.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
@NoArgsConstructor
public class NoteCreateRequest {
	@NotBlank(message = "Title is mandatory")
	private String title;

	@NotBlank(message = "Folder is mandatory")
	private String folder;

	@NotBlank(message = "File is mandatory")
	private MultipartFile file;
}
