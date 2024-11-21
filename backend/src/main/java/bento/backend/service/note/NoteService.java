package bento.backend.service.note;

import bento.backend.repository.NoteRepository;
import bento.backend.repository.AudioRepository;
import bento.backend.repository.SummaryRepository;
import bento.backend.domain.AudioStatus;
import bento.backend.domain.Audio;
import bento.backend.domain.User;
import bento.backend.domain.Note;
import bento.backend.domain.Summary;
import bento.backend.dto.response.NoteListResponse;
import bento.backend.dto.response.NoteDetailResponse;
import bento.backend.dto.response.NoteSummaryResponse;
import bento.backend.dto.response.MessageResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import org.json.simple.JSONObject;

@Service
@RequiredArgsConstructor
public class NoteService {
	private final NoteRepository noteRepository;
	private final AudioRepository audioRepository;
	private final SummaryRepository summaryRepository;

	// 노트 생성
	public MessageResponse createNote(User user, String filePath) {
		// dummy data
		Audio audio = Audio.builder()
				.filePath(filePath)
				.duration("01:30:00")
				.uploadDate(LocalDateTime.now())
				.status(AudioStatus.PROCESSING)
				.language("ko")
				.user(user)
				.build();

		audioRepository.save(audio);

		// TODO : AI로 API 보내기 (Naver Speech-to-Text API, Prompting)

		// dummy data
		Map<String, Object> content = new HashMap<>();

		List<String> speaker = new ArrayList<>();
		speaker.add("Speaker 1");
		speaker.add("Speaker 2");

		List<Map<String, Object>> script = new ArrayList<>();
		script.add(Map.of("speaker", "Speaker 1", "text", "Script 1", "timestamp", "00:00:00", "memo", "Memo 1"));
		script.add(Map.of("speaker", "Speaker 2", "text", "Script 2", "timestamp", "00:01:00", "memo", "Memo 2"));

		content.put("speaker", speaker);
		content.put("script", script);

		JSONObject jsonContent = new JSONObject(content);

		// dummy data
		Note note = Note.builder()
				.title("dummy title")
				.content(jsonContent.toString())
				.folder("dummy folder")
				.audio(audio)
				.user(user)
				.build();

		noteRepository.save(note);

		return MessageResponse.builder()
				.message("Note created successfully")
				.build();
	}

	// 노트 목록 조회
	public List<NoteListResponse> getNoteList(User user) {
		List<Note> notes = noteRepository.findAllByUser(user);
		List<NoteListResponse> noteList = new ArrayList<>();

		for (Note note : notes) {
			Audio audio = note.getAudio();

			NoteListResponse noteListResponse = NoteListResponse.builder()
					.noteId(note.getNoteId())
					.title(note.getTitle())
					.folder(note.getFolder())
					.createdAt(note.getFormattedDateTime(note.getCreatedAt()))
					.duration(audio.getDuration())
					.build();

			noteList.add(noteListResponse);
		}

		return noteList;
	}

	// 노트 상세 조회
	public NoteDetailResponse getNoteDetail(User user, Long noteId) {
		Note note = noteRepository.findByNoteIdAndUser(noteId, user)
			.orElseThrow(() -> new IllegalArgumentException("Note not found"));

		Audio audio = note.getAudio();
		
		return NoteDetailResponse.builder()
				.noteId(note.getNoteId())
				.title(note.getTitle())
				.folder(note.getFolder())
				.createdAt(note.getFormattedDateTime(note.getCreatedAt()))
				.duration(audio.getDuration())
				.content(note.getContent())
				// TODO : AI 응답 형식 보고 수정 예정
				// .speakers(note.getSpeakers())
				// .scripts(note.getScripts())
				.build();
	}

	// 노트 삭제
	public MessageResponse deleteNote(User user, Long noteId) {
		Note note = noteRepository.findByNoteIdAndUser(noteId, user)
				.orElseThrow(() -> new IllegalArgumentException("Note not found"));
		noteRepository.delete(note);

		return MessageResponse.builder()
				.message("Note deleted successfully")
				.build();
	}

	// 노트 수정
	public MessageResponse updateNote(User user, Long noteId, Note noteInfo) {
		Note note = noteRepository.findByNoteIdAndUser(noteId, user)
				.orElseThrow(() -> new IllegalArgumentException("Note not found"));

		note.update(noteInfo.getTitle(), noteInfo.getFolder());
		noteRepository.save(note);

		return MessageResponse.builder()
				.message("Note updated successfully")
				.build();
	}

	// AI 요약
	public NoteSummaryResponse getSummary(User user, Long noteId) {
		Note note = noteRepository.findByNoteIdAndUser(noteId, user)
				.orElseThrow(() -> new IllegalArgumentException("Note not found"));

		Summary summary = note.getSummary();

		if (summary == null) {
			// TODO : AI 요약 API 호출
			// dummy data
			summary = Summary.builder()
					.content("dummy summary")
					.summaryDate(LocalDateTime.now())
					.note(note)
					.build();

			summaryRepository.save(summary);
		}

		return NoteSummaryResponse.builder()
				.summary(summary.getContent())
				.build();
	}
}
