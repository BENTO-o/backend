package bento.backend.service.note;

import bento.backend.constant.ErrorMessages;
import bento.backend.domain.*;
import bento.backend.dto.converter.StringListJsonConverter;
import bento.backend.dto.converter.StringObjectMapJsonConverter;
import bento.backend.dto.request.BookmarkCreateRequest;
import bento.backend.dto.request.MemoCreateRequest;
import bento.backend.dto.response.*;
import bento.backend.repository.NoteRepository;
import bento.backend.repository.AudioRepository;
import bento.backend.repository.SummaryRepository;
import bento.backend.repository.FolderRepository;
import bento.backend.dto.request.NoteCreateRequest;
import bento.backend.dto.request.NoteUpdateRequest;
import bento.backend.exception.ResourceNotFoundException;
import bento.backend.exception.ValidationException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.core.io.ByteArrayResource;

import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
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
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // 비동기 처리를 위한 스레드 풀

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

        Note note = Note.builder()
                .title(request.getTitle())
                .folder(folder)
                .audio(audio)
                .user(user)
                .content("{}") // 빈 JSON 객체로 초기화
                .status(NoteStatus.PROCESSING) // 초기 상태
                .build();

        // Topics, Bookmarks, Memos 처리
        List<String> topics;
        List<BookmarkCreateRequest> bookmarkRequests;
        List<MemoCreateRequest> memoRequests;

        // Default empty JSON arrays if not provided
        String topicsJson = (request.getTopics() == null) ? "[]" : request.getTopics();
        String bookmarkJson = (request.getBookmarks() == null) ? "[]" : request.getBookmarks();
        String memoJson = (request.getMemos() == null) ? "[]" : request.getMemos();

        StringListJsonConverter ListJsonConverter = new StringListJsonConverter();
        topics = ListJsonConverter.convertToEntityAttribute(topicsJson);

        // ObjectMapper를 이용해 BookmarkCreateRequest와 MemoCreateRequest로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            bookmarkRequests = objectMapper.readValue(bookmarkJson, new TypeReference<List<BookmarkCreateRequest>>() {
            });
            memoRequests = objectMapper.readValue(memoJson, new TypeReference<List<MemoCreateRequest>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON", e);
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

        executorService.submit(() -> processNoteAsync(file, audio, note));

        noteRepository.save(note);
        return MessageResponse.builder()
                .message("Note created successfully")
                .id(note.getNoteId())
                .build();
    }

    private void processNoteAsync(MultipartFile file, Audio audio, Note note) {
        StringObjectMapJsonConverter converter = new StringObjectMapJsonConverter();

        try {
            // AI 서버 호출
            String responseJson = getScriptFromAI(file, audio.getLanguage(), note.getTopics().toString());
            Map<String, Object> responseMap = converter.convertToEntityAttribute(responseJson);

            // Audio 업데이트
            String duration = responseMap.get("duration").toString();
            audio.updateDuration(duration);
            audio.updateStatus(AudioStatus.COMPLETED);
            audioRepository.save(audio);

            // Note 업데이트
            Map<String, Object> contentMap = (Map<String, Object>) responseMap.get("content");
            String jsonContent = converter.convertToDatabaseColumn(contentMap);
            note.updateContent(jsonContent);
            note.updateStatus(NoteStatus.COMPLETE);
            noteRepository.save(note);

        } catch (Exception e) {
            e.printStackTrace();
            audio.updateStatus(AudioStatus.FAILED);
            audioRepository.save(audio);
            note.updateStatus(NoteStatus.FAILED);
            noteRepository.save(note);
        }
    }

    // AI 서버로 요청 보내기
    private String getScriptFromAI(MultipartFile file, String language, String topicsJson) {
        String uri = "/scripts"; // STT 요청 URI

        if (topicsJson == null) {
            topicsJson = "[]";
        }

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
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
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

    // 노트 조회
    public Note getNoteById(Long noteId) {
        return noteRepository.findByNoteId(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
    }

    // 조회 Util 함수 : Note → NoteListResponse 변환
    private NoteListResponse convertToNoteListResponse(Note note) {
        Audio audio = note.getAudio();

        return NoteListResponse.builder()
                .noteId(note.getNoteId())
                .title(note.getTitle())
                .folder(note.getFolder() != null ? note.getFolder().getFolderName() : "Default Folder")
                .createdAt(note.getCreatedAt() != null ? note.getFormattedDateTime(note.getCreatedAt()) : "N/A")
                .duration(audio != null ? audio.getDuration() : "00:00:00")
                .build();
    }

    // 노트 목록 조회
    public List<NoteListResponse> getNoteList(User user) {
        List<Note> notes = noteRepository.findAllByUser(user);
        return notes.stream()
                .map(this::convertToNoteListResponse)
                .toList();
    }

    // 노트 목록 조회 (폴더별)
    public List<NoteListResponse> getNoteListByFolder(User user, Long folderId) {
        Folder folder = folderRepository.findByFolderIdAndUser(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));

        List<Note> notes = folder.getNotes();
        return notes.stream()
                .map(this::convertToNoteListResponse)
                .toList();
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
                .id(newFolder.getFolderId())
                .build();
    }

    // 노트 상세 조회
    public NoteDetailResponse getNoteDetail(User user, Long noteId) {
        Note note = noteRepository.findByNoteIdAndUser(noteId, user)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.NOTE_ID_NOT_FOUND_ERROR + noteId));

        if (note.getStatus() == NoteStatus.PROCESSING) {
            throw new ResourceNotFoundException(ErrorMessages.NOTE_PROCESSING);
        }
        else if (note.getStatus() == NoteStatus.FAILED) {
            throw new ResourceNotFoundException(ErrorMessages.NOTE_FAILED);
        }

        Audio audio = note.getAudio();
        StringObjectMapJsonConverter converter = new StringObjectMapJsonConverter();

        Map<String, Object> originalContent;
        try {
            originalContent = converter.convertToEntityAttribute(note.getContent());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Invalid content format for note with ID: " + noteId);
        }

        // script 변환
        Object script = originalContent.get("script");
        if (!(script instanceof List)) {
            throw new ResourceNotFoundException("Invalid script format for note with ID: " + noteId);
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> transformedScript = transformScript((List<Map<String, Object>>) originalContent.get("script"), note);
        Map<String, Object> transformedContent = new HashMap<>();
        transformedContent.put("script", transformedScript);
        transformedContent.put("speaker", originalContent.get("speaker"));
        JsonNode transformedContentNode = objectMapper.valueToTree(transformedContent);
        List<String> summary = note.getSummary() != null ? List.of(note.getSummary().getContent()) : List.of();

        return NoteDetailResponse.builder()
                .noteId(note.getNoteId())
                .title(note.getTitle())
                .folder(note.getFolder().getFolderName())
                .createdAt(note.getFormattedDateTime(note.getCreatedAt()))
                .duration(audio.getDuration())
                .content(transformedContentNode)
                .topics(note.getTopics())
                .bookmarks(transformBookmarks(note.getBookmarks()))
                .memos(transformMemos(note.getMemos()))
                .AI(summary)
                .build();
    }

    private List<Map<String, Object>> transformScript(List<Map<String, Object>> originalScript, Note note) {
        return originalScript.stream().map(entry -> transformScriptEntry(entry, note)).toList();
    }

    private Map<String, Object> transformScriptEntry(Map<String, Object> entry, Note note) {
        String timestamp = (String) entry.get("timestamp");
        return new HashMap<>(entry) {{
            put("memo", findMemoForTimestamp(timestamp, note));
            put("bookmark", isBookmark(timestamp, note));
        }};
    }

    private String findMemoForTimestamp(String timestamp, Note note) {
        return note.getMemos().stream()
                .filter(memo -> memo.getTimestamp().equals(timestamp))
                .map(Memo::getText)
                .findFirst()
                .orElse("");
    }

    private boolean isBookmark(String timestamp, Note note) {
        return note.getBookmarks().stream()
                .anyMatch(bookmark -> bookmark.getTimestamp().equals(timestamp));
    }

    private <T> List<Map<String, String>> transformList(List<T> items, Function<T, Map<String, String>> mapper) {
        return items.stream().map(mapper).toList();
    }

    private List<Map<String, String>> transformBookmarks(List<Bookmark> bookmarks) {
        return transformList(bookmarks, bookmark -> Map.of("timestamp", bookmark.getTimestamp()));
    }

    private List<Map<String, String>> transformMemos(List<Memo> memos) {
        return transformList(memos, memo -> Map.of("timestamp", memo.getTimestamp(), "text", memo.getText()));
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
            String summaryJson = getSummaryFromAI(note);
            StringObjectMapJsonConverter converter = new StringObjectMapJsonConverter();
            Map<String, Object> summaryMap = converter.convertToEntityAttribute(summaryJson);

            summary = Summary.builder()
                    .content(summaryMap.get("summary").toString())
                    .summaryDate(LocalDateTime.now())
                    .note(note)
                    .build();

            summaryRepository.save(summary);
        }

        return NoteSummaryResponse.builder()
                .summary(summary.getContent())
                .build();
    }

    // AI 요약 요청 보내기
    private String getSummaryFromAI(Note note) {
        String uri = "/summarys"; // 요약 요청 URI

        try {
            // Note 객체에서 content와 topics 필드만 추출하여 Map 생성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("content", note.getContent());
            requestBody.put("topics", note.getTopics());

            // WebClient를 사용하여 파일 전송
            String responseBody = webClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody)) // JSON 형식으로 Map을 직접 바디에 추가
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        throw new RuntimeException("Error from AI Server: " + errorMessage);
                                    }))
                    .bodyToMono(String.class)
                    .block(); // TODO : 비동기 처리로 변환

            return responseBody;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send summary request", e);
        }
    }

//    public boolean isNoteOwner(User user, Long noteId) {
//        Optional<Long> ownerId = noteRepository.findUserIdByNoteId(noteId);
//        return (user.getRole().equals(Role.ROLE_ADMIN) || (ownerId != null && ownerId.equals(user.getUserId())));
//    }

    // 노트 검색
    public List<NoteSearchResponse> searchNotes(User user, String query, String startDate, String endDate) {
        if (startDate != null && endDate != null) {
            List<Note> notes = getNotesByDateRange(user, startDate, endDate);

            return notes.stream()
                    .map(note -> NoteSearchResponse.builder()
                            .noteId(note.getNoteId())
                            .title(note.getTitle())
                            .folder(note.getFolder() != null ? note.getFolder().getFolderName() : null)
                            .createdAt(note.getFormattedDateTime(note.getCreatedAt()))
                            .matches(List.of()) // 날짜 검색에서는 matches를 빈 리스트로 반환
                            .build())
                    .toList();
        }

        if (query == null || query.trim().isEmpty()) {
            throw new ValidationException(ErrorMessages.EMPTY_QUERY);
        }

        List<Note> notes = noteRepository.findByQueryAndUser(query, user);

        return notes.stream()
                .map(note -> {
                    List<NoteContentMatch> matches = findMatches(note.getContent(), query);

                    return matches.isEmpty() ? null : NoteSearchResponse.builder()
                            .noteId(note.getNoteId())
                            .title(note.getTitle())
                            .folder(note.getFolder() != null ? note.getFolder().getFolderName() : null)
                            .createdAt(note.getFormattedDateTime(note.getCreatedAt()))
                            .matches(matches) // matches는 ContentMatch 리스트
                            .build();
                })
                .filter(Objects::nonNull) // matches가 없는 노트는 제외
                .toList();
    }

    // 날짜 범위로 노트 조회
    public List<Note> getNotesByDateRange(User user, String startDate, String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // LocalDate로 파싱
            LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
            LocalDate endLocalDate = LocalDate.parse(endDate, formatter);
            // LocalDate를 LocalDateTime으로 변환
            LocalDateTime start = startLocalDate.atStartOfDay(); // 하루 시작 시간 (00:00:00)
            LocalDateTime end = endLocalDate.atTime(23, 59, 59); // 하루 끝 시간 (23:59:59)

            return noteRepository.findByCreatedAtBetweenAndUser(start, end, user);
        } catch (DateTimeParseException e) {
            throw new ValidationException(ErrorMessages.INVALID_DATE_FORMAT);
        }
    }

    // 노트 내용에서 검색어 찾기
    public List<NoteContentMatch> findMatches(String content, String query) {
        if (content == null || content.isEmpty() || query == null || query.isEmpty()) {
            return List.of();
        }

        StringObjectMapJsonConverter converter = new StringObjectMapJsonConverter();

        try {
            // JSON 문자열을 Map<String, Object>로 변환
            Map<String, Object> jsonData = converter.convertToEntityAttribute(content);

            // "script" 배열에서 "text" 검색
            Object scriptObj = jsonData.get("script");
            if (!(scriptObj instanceof List)) {
                return List.of(); // "script"가 없거나 올바른 타입이 아니면 빈 리스트 반환
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> scripts = (List<Map<String, Object>>) scriptObj;

            return scripts.stream()
                    .filter(script -> script.containsKey("text") && script.containsKey("timestamp"))
                    .map(script -> {
                        String text = (String) script.get("text");
                        String timestamp = (String) script.get("timestamp");
                        if (text != null && text.toLowerCase().contains(query.toLowerCase())) {
                            return NoteContentMatch.builder()
                                    .text(text)
                                    .timestamp(timestamp)
                                    .build();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON content", e);
        }
    }

    // 노트 상태 조회
    public NoteStatusResponse getNoteStatus(User user, Long noteId) {
        Note note = noteRepository.findByNoteIdAndUser(noteId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        return NoteStatusResponse.builder()
                .status(note.getStatus().name())
                .build();
    }
}
