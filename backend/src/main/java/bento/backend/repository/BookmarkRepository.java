package bento.backend.repository;

import bento.backend.domain.Bookmark;
import bento.backend.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByNote_NoteId(Long noteId);
    List<Bookmark> findByNote_NoteIdOrderByTimestampDesc(Long noteId);

    List<Bookmark> findAllByNoteUserUserId(Long userId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Bookmark b WHERE b.bookmarkId = :bookmarkId AND (b.note.user.userId = :userId OR :isAdmin = TRUE)")
    boolean existsByIdAndUserOrAdmin(@Param("bookmarkId") Long bookmarkId,
                                     @Param("userId") Long userId,
                                     @Param("isAdmin") boolean isAdmin);

    boolean existsByNoteAndTimestamp(Note note, String timestampStr);


    // 노트별 북마크 개수 집계
//    @Query("SELECT b.note.noteId, COUNT(b) FROM Bookmark b GROUP BY b.note.noteId")
//    List<Object[]> countBookmarksByNote();
}

