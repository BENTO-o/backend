package bento.backend.repository;

import bento.backend.domain.Note;
import bento.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
	Optional<Note> findByNoteIdAndUser(Long noteId, User user);
	List<Note> findAllByUser(User user);
	List<Note> findAllByUserAndFolder(User user, String folder);
}
