package bento.backend.repository;

import bento.backend.domain.Note;
import bento.backend.domain.User;
import bento.backend.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
	Optional<Note> findByNoteIdAndUser(Long noteId, User user);
	Optional<Note> findByNoteId(Long noteId);
	List<Note> findAllByUser(User user);
	List<Note> findAllByUserAndFolder(User user, Folder folder);
	@Query("SELECT n.user.userId FROM Note n WHERE n.noteId = :noteId")
	Optional<Long> findUserIdByNoteId(@Param("noteId") Long noteId);

	@Query("SELECT n FROM Note n WHERE n.user = :user AND n.createdAt BETWEEN :startDate AND :endDate")
	List<Note> findByCreatedAtBetweenAndUser(
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate,
			@Param("user") User user);

	@Query("SELECT n FROM Note n WHERE n.user = :user AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :query, '%')))")
	List<Note> findByQueryAndUser(
			@Param("query") String query,
			@Param("user") User user);
}
