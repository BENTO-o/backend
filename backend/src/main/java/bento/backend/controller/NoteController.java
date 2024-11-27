package bento.backend.controller;

import bento.backend.dto.request.BookmarkCreateRequest;
import bento.backend.dto.request.MemoCreateRequest;
import bento.backend.service.auth.AuthService;
import bento.backend.service.note.NoteService;
import bento.backend.service.file.FileService;
import bento.backend.domain.Note;
import bento.backend.domain.User;
import bento.backend.domain.Folder;
import bento.backend.repository.NoteRepository;
import bento.backend.dto.response.NoteListResponse;
import bento.backend.dto.response.NoteDetailResponse;
import bento.backend.dto.response.MessageResponse;
import bento.backend.dto.response.FolderResponse;
import bento.backend.dto.response.NoteSummaryResponse;
import bento.backend.dto.request.NoteCreateRequest;
import bento.backend.dto.request.NoteUpdateRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;


@RestController
@RequiredArgsConstructor
@RequestMapping("/notes")
public class NoteController {
	public final AuthService authService;
	public final NoteService noteService;
	public final FileService fileService;

	// 테스트용 API
	// 파일 다운로드
	@GetMapping("/download/{fileName}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
		return fileService.downloadFile(fileName);
	}

	// 노트 생성
	@PostMapping("")
	public ResponseEntity<MessageResponse> createNote(
			@RequestHeader("Authorization") String token,
			@RequestPart(value = "file", required = true) MultipartFile file,
			@RequestPart(value = "note") NoteCreateRequest request,
			@RequestPart(value = "bookmarks", required = false) String bookmarksJson,
			@RequestPart(value = "memos", required = false) String memosJson
	) {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			// Parse bookmarks and memos
			List<BookmarkCreateRequest> bookmarks = objectMapper.readValue(
					bookmarksJson,
					new TypeReference<List<BookmarkCreateRequest>>() {}
			);
			List<MemoCreateRequest> memos = objectMapper.readValue(
					memosJson,
					new TypeReference<List<MemoCreateRequest>>() {}
			);

			// Set parsed data
			request.setBookmarks(bookmarks);
			request.setMemos(memos);
			request.setFile(file);

		} catch (JsonProcessingException e) {
			return ResponseEntity.badRequest()
					.body(new MessageResponse("Invalid JSON format: " + e.getMessage()));
		}

		User user = authService.getUserFromToken(token.replace("Bearer ", ""));

		String filePath = fileService.uploadFile(request.getFile());

		return ResponseEntity.status(201).body(noteService.createNote(user, filePath, request));
	}

	// 노트 목록 조회
	@GetMapping("")
	public ResponseEntity<List<NoteListResponse>> getNote(@RequestHeader("Authorization") String token) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));

		return ResponseEntity.status(200).body(noteService.getNoteList(user));
	}

	// 노트 목록 조회 (폴더별)
	@GetMapping("/folders/{folderId}")
	public ResponseEntity<List<NoteListResponse>> getNoteByFolder(@RequestHeader("Authorization") String token, @PathVariable Long folderId) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));

		return ResponseEntity.status(200).body(noteService.getNoteListByFolder(user, folderId));
	}

	// 유저의 폴더 목록 조회
	@GetMapping("/folders")
	public ResponseEntity<List<FolderResponse>> getFolders(@RequestHeader("Authorization") String token) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));

		return ResponseEntity.status(200).body(noteService.getFolders(user));
	}

	// 폴더 생성
	@PostMapping("/folders")
	public ResponseEntity<MessageResponse> createFolder(@RequestHeader("Authorization") String token, @RequestBody Folder folderInfo) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));

		return ResponseEntity.status(201).body(noteService.createFolder(user, folderInfo.getFolderName()));
	}

	// 노트 상세 조회
	@GetMapping("/{noteId}")
	public ResponseEntity<NoteDetailResponse> getNoteDetail(@RequestHeader("Authorization") String token, @PathVariable Long noteId) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));

		return ResponseEntity.status(200).body(noteService.getNoteDetail(user, noteId));
	}

	// 노트 삭제
	@DeleteMapping("/{noteId}")
	public ResponseEntity<MessageResponse> deleteNote(@RequestHeader("Authorization") String token, @PathVariable Long noteId) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));

		return ResponseEntity.status(200).body(noteService.deleteNote(user, noteId));
	}

	// 노트 수정
	@PatchMapping("/{noteId}")
	public ResponseEntity<MessageResponse> updateNote(@RequestHeader("Authorization") String token, @PathVariable Long noteId, @RequestBody NoteUpdateRequest request) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));

		return ResponseEntity.status(200).body(noteService.updateNote(user, noteId, request));
	}

	// AI 요약
	@GetMapping("/{noteId}/ai-summary")
	public ResponseEntity<NoteSummaryResponse> getSummary(@RequestHeader("Authorization") String token, @PathVariable Long noteId) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));

		return ResponseEntity.status(200).body(noteService.getSummary(user, noteId));
	}
}
