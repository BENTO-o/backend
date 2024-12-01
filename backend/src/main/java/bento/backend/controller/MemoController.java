package bento.backend.controller;

import bento.backend.constant.ErrorMessages;
import bento.backend.constant.SuccessMessages;
import bento.backend.domain.Note;
import bento.backend.domain.User;
import bento.backend.dto.request.MemoCreateRequest;
import bento.backend.dto.response.MemoResponse;
import bento.backend.exception.ForbiddenException;
import bento.backend.service.memo.MemoService;
import bento.backend.service.note.NoteService;
import bento.backend.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/memos")
public class MemoController {
    private final MemoService memoService;
    private final NoteService noteService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createMemo(@Valid @RequestBody MemoCreateRequest request) throws Exception {
//        if (!noteService.isNoteOwner(user, request.getNoteId())) {
//            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
//        }
        memoService.createMemo(request);
        Map<String, String> response = Map.of("message", SuccessMessages.MEMO_CREATED);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/note/{noteId}")
    public ResponseEntity<List<MemoResponse>> getMemosByNoteId(@AuthenticationPrincipal Long userId, @PathVariable Long noteId) {
        Note note = noteService.getNoteById(noteId);
        if (!note.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
        }
        List<MemoResponse> response = memoService.getMemosByNoteId(noteId);
        return ResponseEntity.status(200).body(response);
    }

    @PatchMapping("/{memoId}")
    public ResponseEntity<Map<String, String>> updateMemo(@AuthenticationPrincipal Long userId, @PathVariable Long memoId, @Valid @RequestBody MemoCreateRequest request) {
        User user = userService.getUserById(userId);
        if (!memoService.isMemoOwner(user, memoId)) {
            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
        }
        memoService.updateMemo(memoId, request);
        Map<String, String> response = Map.of("message", SuccessMessages.MEMO_UPDATED);
        return ResponseEntity.status(200).body(response);
    }

    @DeleteMapping("/{memoId}")
    public ResponseEntity<Map<String, String>> deleteMemo(@AuthenticationPrincipal Long userId, @PathVariable Long memoId) {
        User user = userService.getUserById(userId);
        if (!memoService.isMemoOwner(user, memoId)) {
            throw new ForbiddenException(ErrorMessages.UNAUTHORIZED_ERROR);
        }
        memoService.deleteMemo(memoId);
        Map<String, String> response = Map.of("message", SuccessMessages.MEMO_DELETED);
        return ResponseEntity.status(200).body(response);
    }
}
