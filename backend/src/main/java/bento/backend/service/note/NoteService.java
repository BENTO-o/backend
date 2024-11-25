package bento.backend.service.note;

import bento.backend.domain.*;
import bento.backend.repository.NoteRepository;
import bento.backend.repository.AudioRepository;
import bento.backend.repository.SummaryRepository;
import bento.backend.repository.FolderRepository;
import bento.backend.dto.response.NoteListResponse;
import bento.backend.dto.response.NoteDetailResponse;
import bento.backend.dto.response.NoteSummaryResponse;
import bento.backend.dto.response.MessageResponse;
import bento.backend.dto.response.FolderResponse;
import bento.backend.dto.request.NoteCreateRequest;
import bento.backend.dto.request.NoteUpdateRequest;
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
	private final FolderRepository folderRepository;
	private final ObjectMapper objectMapper;

	// 노트 생성
	public MessageResponse createNote(User user, String filePath, NoteCreateRequest request) {
		if (request.getFolder() == null) {
			request.setFolder("default");
		}

		Folder folder = folderRepository.findByFolderNameAndUser(request.getFolder(), user)
			.orElseThrow(() -> new ResourceNotFoundException("Folder not found"));

		// TODO : AI로 API 보내기 (Naver Speech-to-Text API, Prompting)
		String duration = "01:00:00"; // TODO : STT API로 받아오기
		String language = "enko";

		Audio audio = Audio.builder()
				.filePath(filePath)
				.duration(duration)
				.status(AudioStatus.PROCESSING)
				.language(language)
				.user(user)
				.build();

		audioRepository.save(audio);


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

		Note note = Note.builder()
				.title(request.getTitle())
				.content(jsonContent.toString())
				.folder(folder)
				.audio(audio)
				.user(user)
				.build();

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
					.folder(note.getFolder().getFolderName())
					.createdAt(note.getFormattedDateTime(note.getCreatedAt()))
					.duration(audio.getDuration())
					.build();

			noteList.add(noteListResponse);
		}

		return noteList;
	}

	// 노트 목록 조회 (폴더별)
	public List<NoteListResponse> getNoteListByFolder(User user, Long folderId) {
		Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
				.orElseThrow(() -> new ResourceNotFoundException("Folder not found"));

		List<Note> notes = folder.getNotes();
		List<NoteListResponse> noteList = new ArrayList<>();

		for (Note note : notes) {
			Audio audio = note.getAudio();

			NoteListResponse noteListResponse = NoteListResponse.builder()
					.noteId(note.getNoteId())
					.title(note.getTitle())
					.folder(note.getFolder().getFolderName())
					.createdAt(note.getFormattedDateTime(note.getCreatedAt()))
					.duration(audio.getDuration())
					.build();

			noteList.add(noteListResponse);
		}

		return noteList;
	}

	// 유저의 폴더 목록 조회
	public List<FolderResponse> getFolders(User user) {
		List<Folder> folders = folderRepository.findAllByUser(user);

		return folders.stream()
				.map(folder -> FolderResponse.builder()
						.folderId(folder.getFolderId())
						.folderName(folder.getFolderName())
						.build())
				.collect(Collectors.toList());
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
				.folder(note.getFolder().getFolderName())
				.createdAt(note.getFormattedDateTime(note.getCreatedAt()))
				.duration(audio.getDuration())
				.content(JsonContent)
				// TODO : AI 응답 형식 보고 수정 예정
				// .speakers(note.getSpeakers())
				// .scripts(note.getScripts())
				.build();
	}

	// TODO : delete x, delete 폴더로 옮기기
	// 노트 삭제
	public MessageResponse deleteNote(User user, Long noteId) {
		Note note = noteRepository.findByNoteIdAndUser(noteId, user)
				.orElseThrow(() -> new ResourceNotFoundException("Note not found"));
		noteRepository.delete(note);

		return MessageResponse.builder()
				.message("Note deleted successfully")
				.build();
	}

	// TODO : 카테고리 변경 기능 추가 (변경 시 GPT 프롬프팅 다시 요청)

	// TODO : 스크립트 내용도 수정 가능하도록 변경
	// 노트 수정
	public MessageResponse updateNote(User user, Long noteId, NoteUpdateRequest request) {
		Note note = noteRepository.findByNoteIdAndUser(noteId, user)
				.orElseThrow(() -> new ResourceNotFoundException("Note not found"));

		if (request.getTitle() != null) {
			note.updateTitle(request.getTitle());
		}

		if (request.getFolderName() != null) {
			Folder newFolder = folderRepository.findByFolderNameAndUser(request.getFolderName(), user)
					.orElseThrow(() -> new ResourceNotFoundException("Folder not found"));

			note.updateFolder(newFolder);
		}

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
