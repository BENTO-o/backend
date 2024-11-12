package bento.backend.controller;

import bento.backend.domain.Note;
import bento.backend.dto.response.NoteListResponse;
import bento.backend.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notes")
public class NoteController {
	private final NoteRepository noteRepository;

	// 노트 생성
	@PostMapping("")
	public void createNote() {
		noteRepository.save(Note.builder()
				.title("Title")
				.folder("Folder")
				.content("Content")
				.build());
	}

	// 노트 목록 조회
	@GetMapping("")
	public List<NoteListResponse> getNote() {
		List<Note> notes = noteRepository.findAll();
		return notes.stream()
				.map(note -> NoteListResponse.builder()
						.noteId(note.getNoteId())
						.title(note.getTitle())
						.folder(note.getFolder())
						.createdAt(note.getFormattedDateTime(note.getCreatedAt()))
						.duration("01:30:00") // 임시 데이터
						.build())
				.collect(Collectors.toList());
	}
}
