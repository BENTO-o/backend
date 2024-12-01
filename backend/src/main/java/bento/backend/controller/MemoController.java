package bento.backend.controller;

import bento.backend.constant.ErrorMessages;
import bento.backend.constant.SuccessMessages;
import bento.backend.domain.Note;
import bento.backend.domain.User;
import bento.backend.dto.request.MemoCreateRequest;
import bento.backend.dto.response.BookmarkResponse;
import bento.backend.dto.response.MemoResponse;
import bento.backend.exception.ForbiddenException;
import bento.backend.service.auth.AuthService;
import bento.backend.service.memo.MemoService;
import bento.backend.service.note.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/memos")
public class MemoController {
    private final MemoService memoService;
    private final NoteService noteService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createMemo(@RequestHeader("Authorization") String token, @Valid @RequestBody MemoCreateRequest request) throws Exception {
        User user = authService.getUserFromToken(token.replace("Bearer ", ""));
//        if (!noteService.isNoteOwner(user, request.getNoteId())) {
//            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
//        }
        memoService.createMemo(request);
        Map<String, String> response = Map.of("message", SuccessMessages.MEMO_CREATED);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/note/{noteId}")
    public ResponseEntity<List<MemoResponse>> getMemosByNoteId(@RequestHeader("Authorization") String token, @PathVariable Long noteId) {
        User user = authService.getUserFromToken((token.replace("Bearer ", "")));
        Note note = noteService.getNoteById(noteId);
        if (!note.getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
        }
        List<MemoResponse> response = memoService.getMemosByNoteId(noteId);
        return ResponseEntity.status(200).body(response);
    }

    @PatchMapping("/{memoId}")
    public ResponseEntity<Map<String, String>> updateMemo(@RequestHeader("Authorization") String token, @PathVariable Long memoId, @Valid @RequestBody MemoCreateRequest request) {
        User user = authService.getUserFromToken(token.replace("Bearer ", ""));
        if (!memoService.isMemoOwner(user, memoId)) {
            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
        }
        memoService.updateMemo(memoId, request);
        Map<String, String> response = Map.of("message", SuccessMessages.MEMO_UPDATED);
        return ResponseEntity.status(200).body(response);
    }

    @DeleteMapping("/{memoId}")
    public ResponseEntity<Map<String, String>> deleteMemo(@RequestHeader("Authorization") String token, @PathVariable Long memoId) {
        User user = authService.getUserFromToken(token.replace("Bearer ", ""));
        if (!memoService.isMemoOwner(user, memoId)) {
            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
        }
        memoService.deleteMemo(memoId);
        Map<String, String> response = Map.of("message", SuccessMessages.MEMO_DELETED);
        return ResponseEntity.status(200).body(response);
    }
}
