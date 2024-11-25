package bento.backend.repository;

import bento.backend.domain.Note;
import bento.backend.domain.User;
import bento.backend.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
	Optional<Note> findByNoteIdAndUser(Long noteId, User user);
	Optional<Note> findByNoteId(Long noteId);
	List<Note> findAllByUser(User user);

    Long findUserIdByNoteId(Long noteId);
	List<Note> findAllByUserAndFolder(User user, Folder folder);
}
