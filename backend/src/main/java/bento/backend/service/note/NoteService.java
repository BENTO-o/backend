package bento.backend.service.note;

import bento.backend.domain.*;
import bento.backend.repository.NoteRepository;
import bento.backend.repository.AudioRepository;
import bento.backend.repository.SummaryRepository;
import bento.backend.dto.response.NoteListResponse;
import bento.backend.dto.response.NoteDetailResponse;
import bento.backend.dto.response.NoteSummaryResponse;
import bento.backend.dto.response.MessageResponse;
import bento.backend.dto.request.NoteCreateRequest;
import bento.backend.exception.ResourceNotFoundException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import org.json.simple.JSONObject;

@Service
@RequiredArgsConstructor
public class NoteService {
	private final NoteRepository noteRepository;
	private final AudioRepository audioRepository;
	private final SummaryRepository summaryRepository;
	private final ObjectMapper objectMapper;

	// 노트 생성
	public MessageResponse createNote(User user, String filePath, NoteCreateRequest request) {
		if (request.getFolder() == null) {
			request.setFolder("default");
		}

		Audio audio = Audio.builder()
				.filePath(filePath)
				.duration(request.getDuration())
				.status(AudioStatus.PROCESSING)
				.language(request.getLanguage())
				.user(user)
				.build();
		audioRepository.save(audio);

		// Prepare content
		Map<String, Object> content = new HashMap<>();
		try {
			List<String> speaker = List.of("Speaker 1", "Speaker 2");
			List<Map<String, Object>> script = List.of(
					Map.of("speaker", "Speaker 1", "text", "Script 1", "timestamp", "00:00:00", "memo", "Memo 1"),
					Map.of("speaker", "Speaker 2", "text", "Script 2", "timestamp", "00:01:00", "memo", "Memo 2")
			);

			content.put("speaker", speaker);
			content.put("script", script);
		} catch (Exception e) {
			// Fallback if there's an issue generating dummy content
			content = Map.of();
		}

		JSONObject jsonContent = new JSONObject(content);

		Note note = Note.builder()
				.title(request.getTitle())
				.content(jsonContent.toString()) // Ensure content is always a valid JSON string
				.folder(request.getFolder())
				.audio(audio)
				.user(user)
				.build();

		// Add bookmarks and memos if present
		if (request.getBookmarks() != null) {
			List<Bookmark> bookmarks = request.getBookmarks().stream()
					.map(bookmarkRequest -> Bookmark.builder()
							.timestamp(bookmarkRequest.getTimestamp())
							.note(note)
							.build())
					.collect(Collectors.toList());
			note.getBookmarks().addAll(bookmarks);
		}

		if (request.getMemos() != null) {
			List<Memo> memos = request.getMemos().stream()
					.map(memoRequest -> Memo.builder()
							.text(memoRequest.getText())
							.timestamp(memoRequest.getTimestamp())
							.note(note)
							.build())
					.collect(Collectors.toList());
			note.getMemos().addAll(memos);
		}

		noteRepository.save(note);

		return MessageResponse.builder()
				.message("Note created successfully")
				.build();
	}



	// 노트 조회
    public Note getNoteById(Long noteId) {
		return noteRepository.findByNoteId(noteId)
				.orElseThrow(() -> new ResourceNotFoundException("Note not found"));
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

	// 노트 목록 조회 (폴더별)
	public List<NoteListResponse> getNoteListByFolder(User user, String folder) {
		List<Note> notes = noteRepository.findAllByUserAndFolder(user, folder);

		if (notes.isEmpty()) {
			throw new ResourceNotFoundException("Folder not found");
		}

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

	// 유저의 폴더 목록 조회
	public List<String> getFolders(User user) {
		List<Note> notes = noteRepository.findAllByUser(user);
		List<String> folders = notes.stream()
				.map(Note::getFolder)
				.distinct()
				.collect(Collectors.toList());

		return folders;
	}

	// 노트 상세 조회
	public NoteDetailResponse getNoteDetail(User user, Long noteId) {
		Note note = noteRepository.findByNoteIdAndUser(noteId, user)
			.orElseThrow(() -> new ResourceNotFoundException("Note not found"));

		Audio audio = note.getAudio();
		JsonNode JsonContent;
		try{
			JsonContent = objectMapper.readTree(note.getContent());
		} catch (Exception e) {
			throw new ResourceNotFoundException("Content not found");
		}

		return NoteDetailResponse.builder()
				.noteId(note.getNoteId())
				.title(note.getTitle())
				.folder(note.getFolder())
				.createdAt(note.getFormattedDateTime(note.getCreatedAt()))
				.duration(audio.getDuration())
				.content(JsonContent)
				// TODO : AI 응답 형식 보고 수정 예정
				// .speakers(note.getSpeakers())
				// .scripts(note.getScripts())
				.build();
	}

	// 노트 삭제
	public MessageResponse deleteNote(User user, Long noteId) {
		Note note = noteRepository.findByNoteIdAndUser(noteId, user)
				.orElseThrow(() -> new ResourceNotFoundException("Note not found"));
		noteRepository.delete(note);

		return MessageResponse.builder()
				.message("Note deleted successfully")
				.build();
	}

	// 노트 수정
	public MessageResponse updateNote(User user, Long noteId, Note noteInfo) {
		Note note = noteRepository.findByNoteIdAndUser(noteId, user)
				.orElseThrow(() -> new ResourceNotFoundException("Note not found"));

		note.update(noteInfo.getTitle(), noteInfo.getFolder());
		noteRepository.save(note);

		return MessageResponse.builder()
				.message("Note updated successfully")
				.build();
	}

	// AI 요약
	public NoteSummaryResponse getSummary(User user, Long noteId) {
		Note note = noteRepository.findByNoteIdAndUser(noteId, user)
				.orElseThrow(() -> new ResourceNotFoundException("Note not found"));

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

	public boolean isNoteOwner(User user, Long noteId) {
		Optional<Long> ownerId = noteRepository.findUserIdByNoteId(noteId);
		return (user.getRole().equals(Role.ROLE_ADMIN) || (ownerId != null && ownerId.equals(user.getUserId())));
	}

}
