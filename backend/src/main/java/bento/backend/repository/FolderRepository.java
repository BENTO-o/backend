package bento.backend.repository;

import bento.backend.domain.Note;
import bento.backend.domain.User;
import bento.backend.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
	Optional<Folder> findByFolderIdAndUser(Long folderId, User user);
	Optional<Folder> findByFolderId(Long folderId);
	List<Folder> findAllByUser(User user);
	Optional<Folder> findByFolderNameAndUser(String folderName, User user);
	Boolean existsByFolderNameAndUser(String folderName, User user);


}
