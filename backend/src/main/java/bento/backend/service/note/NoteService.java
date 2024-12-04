package bento.backend.service.note;

import bento.backend.constant.ErrorMessages;
import bento.backend.domain.*;
import bento.backend.dto.request.BookmarkCreateRequest;
import bento.backend.dto.request.MemoCreateRequest;
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
import bento.backend.exception.ValidationException;
import bento.backend.utils.MultipartFileResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import org.json.simple.JSONObject;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.core.io.ByteArrayResource;


import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final AudioRepository audioRepository;
    private final SummaryRepository summaryRepository;
    private final FolderRepository folderRepository;
    private final ObjectMapper objectMapper;
	private final WebClient webClient;

	// 노트 생성
	public MessageResponse createNote(User user, MultipartFile file, String filePath, NoteCreateRequest request) {
        String language = "enko"; // default language

		if (request.getFolder() == null) {
			request.setFolder("default");
		}

        Folder folder = folderRepository.findByFolderNameAndUser(request.getFolder(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));

        Audio audio = Audio.builder()
                .filePath(filePath)
                .duration("00:00:00")
                .status(AudioStatus.PROCESSING)
                .language(language)
                .user(user)
                .build();

        audioRepository.save(audio);

        // AI 서버로 요청 보내기
		String responseJson = getScriptFromAI(file, language, request.getTopics());
        Map<String, Object> responseMap = convertJsonToMap(responseJson);

        // 응답값 바탕으로 duration 설정
        audio.updateDuration(responseMap.get("duration").toString());
        audioRepository.save(audio);

		Map<String, Object> contentMap = (Map<String, Object>) responseMap.get("content");
        String jsonContent = convertObjectToJsonString(contentMap);

		// Note 객체 생성
		Note note = Note.builder()
			.title(request.getTitle())
            .content(jsonContent)
			.folder(folder)
			.audio(audio)
			.user(user)
			.build();

        // Add bookmarks and memos if present
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> topics;
        List<BookmarkCreateRequest> bookmarkRequests;
        List<MemoCreateRequest> memoRequests;

        // Default empty JSON arrays if not provided
        String topicsJson = (request.getTopics() == null) ? "[]" : request.getTopics();
        String bookmarkJson = (request.getBookmarks() == null) ? "[]" : request.getBookmarks();
        String memoJson = (request.getMemos() == null) ? "[]" : request.getMemos();

        try {
            // Parse topics, bookmarks, and memos
            topics = objectMapper.readValue( topicsJson, new TypeReference<>() {} );
            bookmarkRequests = objectMapper.readValue( bookmarkJson, new TypeReference<>() {} );
            memoRequests = objectMapper.readValue( memoJson, new TypeReference<>() {} );
        } catch (JsonProcessingException e) {
            throw new ValidationException(ErrorMessages.INVALID_JSON_FORMAT);
        }
        note.getTopics().addAll(topics);

        List<Bookmark> bookmarks = bookmarkRequests.stream()
                .map(bookmarkRequest -> Bookmark.builder()
                        .timestamp(bookmarkRequest.getTimestamp())
                        .note(note)
                        .build())
                .toList();
        note.getBookmarks().addAll(bookmarks);

        List<Memo> memos = memoRequests.stream()
                .map(memoRequest -> Memo.builder()
                        .text(memoRequest.getText())
                        .timestamp(memoRequest.getTimestamp())
                        .note(note)
                        .build())
                .toList();
        note.getMemos().addAll(memos);

        noteRepository.save(note);
        return MessageResponse.builder()
                .message("Note created successfully")
                .build();
    }

    // AI 서버로 요청 보내기
	private String getScriptFromAI(MultipartFile file, String language, String topicsJson) {
		String uri = "/scripts"; // STT 요청 URI

        try {
            // Multipart 데이터를 MultiValueMap에 설정
            MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
            bodyMap.add("file", new ByteArrayResource(file.getBytes()) {
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            bodyMap.add("language", language);
            bodyMap.add("topics", topicsJson);

            // WebClient를 사용하여 파일 전송
            String responseBody = webClient.post()
                .uri(uri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyMap))
                .retrieve()
                .bodyToMono(String.class)
                .block(); // TODO : 비동기 처리로 변환

            return responseBody;

        } catch (IOException e) {
            throw new RuntimeException("Error reading file input stream", e);
        }
	}

    // 유니코드 응답 디코딩
    private String decodeUnicodeResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Object json = objectMapper.readValue(responseBody, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to decode Unicode response");
        }
    }

    // JSON String to Map 변환
    private Map<String, Object> convertJsonToMap(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to convert JSON string to Map");
        }
    }

    // Object to JSON String 변환
    private String convertObjectToJsonString(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // 맵 데이터를 JSON 문자열로 직렬화
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to convert Map to JSON string");
        }
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

    // 폴더 생성
    public MessageResponse createFolder(User user, String folderName) {
        if (folderName == null) {
            throw new ValidationException("Folder name is required");
        }

        if (folderRepository.existsByFolderNameAndUser(folderName, user)) {
            throw new ValidationException("Folder already exists");
        }

        Folder newFolder = Folder.builder()
                .folderName(folderName)
                .user(user)
                .build();

        folderRepository.save(newFolder);

        return MessageResponse.builder()
                .message("Folder created successfully")
                .build();
    }

    // 노트 상세 조회
    public NoteDetailResponse getNoteDetail(User user, Long noteId) {
        Note note = noteRepository.findByNoteIdAndUser(noteId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        Audio audio = note.getAudio();
        JsonNode JsonContent;
        try {
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

