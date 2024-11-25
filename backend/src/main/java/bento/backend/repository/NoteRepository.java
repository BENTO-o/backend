package bento.backend.repository;

import bento.backend.domain.Note;
import bento.backend.domain.User;
import bento.backend.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
	Optional<Note> findByNoteIdAndUser(Long noteId, User user);
	Optional<Note> findByNoteId(Long noteId);
	List<Note> findAllByUser(User user);
	List<Note> findAllByUserAndFolder(User user, Folder folder);
	@Query("SELECT n.user.userId FROM Note n WHERE n.noteId = :noteId")
	Optional<Long> findUserIdByNoteId(@Param("noteId") Long noteId);
}
