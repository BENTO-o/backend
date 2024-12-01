package bento.backend.service.memo;

import bento.backend.constant.ErrorMessages;
import bento.backend.domain.Memo;
import bento.backend.domain.Note;
import bento.backend.domain.Role;
import bento.backend.domain.User;
import bento.backend.dto.request.MemoCreateRequest;
import bento.backend.dto.response.MemoResponse;
import bento.backend.exception.BadRequestException;
import bento.backend.repository.MemoRepository;
import bento.backend.repository.NoteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoService {
    private final MemoRepository memoRepository;
    private final NoteRepository noteRepository;

    public void createMemo(MemoCreateRequest request) throws Exception {
        Note note = noteRepository.findById(request.getNoteId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.NOTE_ID_NOT_FOUND_ERROR + request.getNoteId()));
        String timestampStr = request.getTimestamp();

        LocalTime timeStamp = LocalTime.parse((timestampStr));
        LocalTime audioLength = LocalTime.parse(note.getAudio().getDuration());
        // Validate that the timestamp is within the audio length
        if (timeStamp.isAfter(audioLength)) {
            throw new BadRequestException(ErrorMessages.MEMO_TIMESTAMP_ERROR);
        }
        // Check if a memo with the same noteId and timestamp already exists
        boolean memoExists = memoRepository.existsByNoteAndTimestamp(note, timestampStr);
        if (memoExists) {
            throw new BadRequestException(ErrorMessages.MEMO_ALREADY_EXISTS_ERROR);
        }

        memoRepository.save(Memo.builder()
                .note(note)
                .timestamp(timestampStr)
                .text(request.getText())
                .build());
    }

    public void updateMemo(Long memoId, MemoCreateRequest request) {
        // note와 메모가 존재하는지 확인
        noteRepository.findById(request.getNoteId())
                .orElseThrow(() -> new BadRequestException((ErrorMessages.NOTE_ID_NOT_FOUND_ERROR + request.getNoteId())));
        Memo memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new BadRequestException(ErrorMessages.MEMO_ID_NOT_FOUND_ERROR + memoId));
        // memo update에서는 timestamp를 변경할 수 없음
        memo.setText(request.getText());
        memoRepository.save(memo);
    }

    public List<MemoResponse> getMemosByNoteId(Long noteId) {
        List<Memo> memos = memoRepository.findByNote_NoteIdOrderByTimestampDesc(noteId);
        return memos.stream()
                .map(memo -> MemoResponse.builder()
                        .memoId(memo.getMemoId())
                        .noteId(memo.getNote().getNoteId())
                        .timestamp(memo.getTimestamp())
                        .text(memo.getText())
                        .build())
                .collect(Collectors.toList());
    }

    public void deleteMemo(Long memoId) {
        Memo memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new BadRequestException(ErrorMessages.MEMO_ID_NOT_FOUND_ERROR + memoId));
        memoRepository.delete(memo);
    }

    public boolean isMemoOwner(User user, Long memoId) {
        return user.getRole().equals(Role.ROLE_ADMIN) || memoRepository.existsByMemoIdAndNote_User_UserId(memoId, user);
    }
}
