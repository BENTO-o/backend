package bento.backend.controller;

import bento.backend.dto.response.*;
import bento.backend.service.note.NoteService;
import bento.backend.service.file.FileService;
import bento.backend.domain.User;
import bento.backend.domain.Folder;
import bento.backend.dto.request.NoteCreateRequest;
import bento.backend.dto.request.NoteUpdateRequest;

import bento.backend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/notes")
public class NoteController {
 	public final UserService userService;
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
			@AuthenticationPrincipal Long userId,
			@ModelAttribute NoteCreateRequest request
	) {
		User user = userService.getUserById(userId);
		String filePath = fileService.uploadFile(request.getFile());

		return ResponseEntity.status(201).body(noteService.createNote(user, request.getFile(), filePath, request));
	}

	// 노트 목록 조회
	@GetMapping("")
	public ResponseEntity<List<NoteListResponse>> getNote(@AuthenticationPrincipal Long userId) {
		User user = userService.getUserById(userId);
		return ResponseEntity.status(200).body(noteService.getNoteList(user));
	}

	// 노트 검색
	@GetMapping("/search")
	public ResponseEntity<List<NoteSearchResponse>> searchNotes(
			@AuthenticationPrincipal Long userId,
			@RequestParam(required = false) String query,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate
	) {
		User user = userService.getUserById(userId);
		List<NoteSearchResponse> results = noteService.searchNotes(user, query, startDate, endDate);
		return ResponseEntity.status(200).body(results);
	}

	// 노트 목록 조회 (폴더별)
	@GetMapping("/folders/{folderId}")
	public ResponseEntity<List<NoteListResponse>> getNoteByFolder(@AuthenticationPrincipal Long userId, @PathVariable Long folderId) {
		User user = userService.getUserById(userId);
		return ResponseEntity.status(200).body(noteService.getNoteListByFolder(user, folderId));
	}

	// 유저의 폴더 목록 조회
	@GetMapping("/folders")
	public ResponseEntity<List<FolderResponse>> getFolders(@AuthenticationPrincipal Long userId) {
		User user = userService.getUserById(userId);
		return ResponseEntity.status(200).body(noteService.getFolders(user));
	}

	// 폴더 생성
	@PostMapping("/folders")
	public ResponseEntity<MessageResponse> createFolder(@AuthenticationPrincipal Long userId, @RequestBody Folder folderInfo) {
		User user = userService.getUserById(userId);
		return ResponseEntity.status(201).body(noteService.createFolder(user, folderInfo.getFolderName()));
	}

	// 노트 상세 조회
	@GetMapping("/{noteId}")
	public ResponseEntity<NoteDetailResponse> getNoteDetail(@AuthenticationPrincipal Long userId, @PathVariable Long noteId) {
		User user = userService.getUserById(userId);
		return ResponseEntity.status(200).body(noteService.getNoteDetail(user, noteId));
	}

	// 노트 삭제
	@DeleteMapping("/{noteId}")
	public ResponseEntity<MessageResponse> deleteNote(@AuthenticationPrincipal Long userId, @PathVariable Long noteId) {
		User user = userService.getUserById(userId);
		return ResponseEntity.status(200).body(noteService.deleteNote(user, noteId));
	}

	// 노트 수정
	@PatchMapping("/{noteId}")
	public ResponseEntity<MessageResponse> updateNote(@AuthenticationPrincipal Long userId, @PathVariable Long noteId, @RequestBody NoteUpdateRequest request) {
		User user = userService.getUserById(userId);
		return ResponseEntity.status(200).body(noteService.updateNote(user, noteId, request));
	}

	// AI 요약
	@GetMapping("/{noteId}/ai-summary")
	public ResponseEntity<NoteSummaryResponse> getSummary(@AuthenticationPrincipal Long userId, @PathVariable Long noteId) {
		User user = userService.getUserById(userId);
		return ResponseEntity.status(200).body(noteService.getSummary(user, noteId));
	}
}
