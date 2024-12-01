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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<Map<String, String>> createBookmark(@Valid @RequestBody BookmarkCreateRequest request) {
//        if (!noteService.isNoteOwner(user, request.getNoteId())) {
//            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
//        }
        bookmarkService.createBookmark(request);
        Map<String, String> response = Map.of("message", SuccessMessages.BOOKMARK_CREATED);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/note/{noteId}")
    public ResponseEntity<List<BookmarkResponse>> getBookmarksByNoteId(@AuthenticationPrincipal Long userId, @PathVariable Long noteId) {
        Note note = noteService.getNoteById(noteId);
        if (!note.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
        }
        List<BookmarkResponse> response = bookmarkService.getBookmarksByNoteId(noteId);
        return ResponseEntity.status(200).body(response);
    }

    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Map<String, String>> deleteBookmark(@PathVariable Long bookmarkId) {
//        if (!bookmarkService.isBookmarkOwner(user, bookmarkId)) {
//            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
//        }
        bookmarkService.deleteBookmark(bookmarkId);
        Map<String, String> response = Map.of("message", SuccessMessages.BOOKMARK_DELETED);
        return ResponseEntity.status(200).body(response);
    }
}
