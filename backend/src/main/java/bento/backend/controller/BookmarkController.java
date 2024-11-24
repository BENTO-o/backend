package bento.backend.controller;

import bento.backend.constant.ErrorMessages;
import bento.backend.constant.SuccessMessages;
import bento.backend.domain.Note;
import bento.backend.domain.User;
import bento.backend.dto.request.BookmarkCreateRequest;
import bento.backend.dto.response.BookmarkResponse;
import bento.backend.exception.BadRequestException;
import bento.backend.exception.ForbiddenException;
import bento.backend.exception.UnauthorizedException;
import bento.backend.repository.BookmarkRepository;
import bento.backend.repository.UserRepository;
import bento.backend.service.auth.AuthService;
import bento.backend.service.bookmark.BookmarkService;
import bento.backend.service.note.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final UserRepository userRepository;
    private final AuthService authService;
    private final BookmarkRepository bookmarkRepository;

    @PostMapping
    public ResponseEntity<Map<String, String>> createBookmark(@RequestHeader("Authorization") String token, @Valid @RequestBody BookmarkCreateRequest request) {
        authService.getUserFromToken(token.replace("Bearer ", ""));
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
        String tokenValue = token.replace("Bearer ", "");
        if (!authService.canDeleteBookmark(tokenValue, bookmarkId)) {
//            TODO : 현재 여러 종류의 예외를 모두 ForbiddenException으로 처리하고 있습니다. 추후에 수정이 필요합니다.
            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
        }
        bookmarkService.deleteBookmark(bookmarkId);
        Map<String, String> response = Map.of("message", SuccessMessages.BOOKMARK_DELETED);
        return ResponseEntity.status(200).body(response);
    }

}
