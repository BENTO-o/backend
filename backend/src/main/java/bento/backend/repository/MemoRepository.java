package bento.backend.repository;

import bento.backend.domain.Memo;
import bento.backend.domain.Note;
import bento.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    boolean existsByNoteAndTimestamp(Note note, String timestampStr);

    List<Memo> findByNote_NoteIdOrderByTimestampDesc(Long noteId);

    boolean existsByIdAndUser(Long memoId, User user);
}
