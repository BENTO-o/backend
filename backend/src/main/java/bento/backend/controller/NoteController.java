package bento.backend.controller;

import bento.backend.service.auth.AuthService;
import bento.backend.service.note.NoteService;
import bento.backend.service.file.FileService;
import bento.backend.domain.Note;
import bento.backend.domain.User;
import bento.backend.repository.NoteRepository;
import bento.backend.dto.response.NoteListResponse;
import bento.backend.dto.response.NoteDetailResponse;
import bento.backend.dto.response.MessageResponse;
import bento.backend.dto.response.NoteSummaryResponse;
import bento.backend.dto.request.NoteCreateRequest;

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
	public ResponseEntity<MessageResponse> createNote(@RequestHeader("Authorization") String token, @ModelAttribute NoteCreateRequest request) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));

		// 파일 업로드
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
	@GetMapping("/folders/{folder}")
	public ResponseEntity<List<NoteListResponse>> getNoteByFolder(@RequestHeader("Authorization") String token, @PathVariable String folder) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));

		return ResponseEntity.status(200).body(noteService.getNoteListByFolder(user, folder));
	}

	// 유저의 폴더 목록 조회
	@GetMapping("/folders")
	public ResponseEntity<List<String>> getFolders(@RequestHeader("Authorization") String token) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));
		List<String> folders = noteService.getFolders(user);

		return ResponseEntity.status(200).body(folders);
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
	public ResponseEntity<MessageResponse> updateNote(@RequestHeader("Authorization") String token, @PathVariable Long noteId, @RequestBody Note noteInfo) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));

		return ResponseEntity.status(200).body(noteService.updateNote(user, noteId, noteInfo));
	}

	// AI 요약
	@GetMapping("/{noteId}/ai-summary")
	public ResponseEntity<NoteSummaryResponse> getSummary(@RequestHeader("Authorization") String token, @PathVariable Long noteId) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));

		return ResponseEntity.status(200).body(noteService.getSummary(user, noteId));
	}
}
