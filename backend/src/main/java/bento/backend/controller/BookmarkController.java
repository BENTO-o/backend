package bento.backend.controller;

import bento.backend.constant.ErrorMessages;
import bento.backend.constant.SuccessMessages;
import bento.backend.domain.Note;
import bento.backend.domain.User;
import bento.backend.dto.request.BookmarkCreateRequest;
import bento.backend.dto.response.BookmarkResponse;
import bento.backend.exception.ForbiddenException;
import bento.backend.service.auth.AuthService;
import bento.backend.service.bookmark.BookmarkService;
import bento.backend.service.note.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookmarks")
public class BookmarkController {
    private final BookmarkService bookmarkService;
    private final NoteService noteService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createBookmark(@RequestHeader("Authorization") String token, @Valid @RequestBody BookmarkCreateRequest request) {
        User user = authService.getUserFromToken(token.replace("Bearer ", ""));
        if (!noteService.isNoteOwner(user, request.getNoteId())) {
            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
        }
        bookmarkService.createBookmark(request);
        Map<String, String> response = Map.of("message", SuccessMessages.BOOKMARK_CREATED);
        return ResponseEntity.status(201).body(response);
    }

    //    일단 만들어두긴 했지만, 사용하지 않을 것 같은 메소드입니다.
    @GetMapping
    public ResponseEntity<List<BookmarkResponse>> getBookmarks(@RequestHeader("Authorization") String token) {
        User user = authService.getUserFromToken(token.replace("Bearer ", ""));
        List<BookmarkResponse> response = bookmarkService.getBookmarks(user);
        return ResponseEntity.status(200).body(response);
    }

    @GetMapping("/note/{noteId}")
    public ResponseEntity<List<BookmarkResponse>> getBookmarksByNoteId(@RequestHeader("Authorization") String token, @PathVariable Long noteId) {
        User user = authService.getUserFromToken(token.replace("Bearer ", ""));
        Note note = noteService.getNoteById(noteId);
        if (!note.getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
        }
        List<BookmarkResponse> response = bookmarkService.getBookmarksByNoteId(noteId);
        return ResponseEntity.status(200).body(response);
    }

    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Map<String, String>> deleteBookmark(@RequestHeader("Authorization") String token, @PathVariable Long bookmarkId) {
        User user = authService.getUserFromToken(token.replace("Bearer ", ""));
        if (!bookmarkService.isBookmarkOwner(user, bookmarkId)) {
            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
        }
        bookmarkService.deleteBookmark(bookmarkId);
        Map<String, String> response = Map.of("message", SuccessMessages.BOOKMARK_DELETED);
        return ResponseEntity.status(200).body(response);
    }

}
