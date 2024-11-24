package bento.backend.service.bookmark;

import bento.backend.constant.ErrorMessages;
import bento.backend.domain.Bookmark;
import bento.backend.domain.Note;
import bento.backend.domain.User;
import bento.backend.dto.request.BookmarkCreateRequest;
import bento.backend.dto.response.BookmarkResponse;
import bento.backend.exception.BadRequestException;
import bento.backend.repository.BookmarkRepository;
import bento.backend.repository.NoteRepository;
import bento.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public void createBookmark(BookmarkCreateRequest request) {
        Note note = noteRepository.findById(request.getNoteId())
                .orElseThrow(() -> new BadRequestException(ErrorMessages.NOTE_ID_NOT_FOUND_ERROR + request.getNoteId()));
        String timestampStr = request.getTimestamp();

        LocalTime timestamp = LocalTime.parse(timestampStr);
        LocalTime audioLength = LocalTime.parse(note.getAudio().getDuration());
        // Validate that the timestamp is within the audio length
        if (timestamp.isAfter(audioLength)) {
            throw new BadRequestException(ErrorMessages.BOOKMARK_TIMESTAMP_ERROR);
        }
        // Check if a bookmark with the same noteId and timestamp already exists
        boolean bookmarkExists = bookmarkRepository.existsByNoteAndTimestamp(note, timestampStr);
        if (bookmarkExists) {
            throw new BadRequestException(ErrorMessages.BOOKMARK_ALREADY_EXISTS_ERROR);
        }

        bookmarkRepository.save(Bookmark.builder()
                .note(note)
                .timestamp(timestampStr)
                .build());
    }

//    일단 만들어두긴 했지만, 사용하지 않을 것 같은 메소드입니다.
    public List<BookmarkResponse> getBookmarks(User user) {
        List<Bookmark> bookmarks = bookmarkRepository.findAllByNoteUserUserId(user.getUserId());
        return bookmarks.stream()
                .map(bookmark -> BookmarkResponse.builder()
                        .bookmarkId(bookmark.getBookmarkId())
                        .noteId(bookmark.getNote().getNoteId())
                        .timestamp(bookmark.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    public List<BookmarkResponse> getBookmarksByNoteId(Long noteId) {
        List<Bookmark> bookmarks = bookmarkRepository.findByNote_NoteIdOrderByTimestampDesc(noteId);
        return bookmarks.stream()
                .map(bookmark -> BookmarkResponse.builder()
                        .bookmarkId(bookmark.getBookmarkId())
                        .noteId(bookmark.getNote().getNoteId())
                        .timestamp(bookmark.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    public void deleteBookmark(Long bookmarkId) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new BadRequestException(ErrorMessages.BOOKMARK_ID_NOT_FOUND_ERROR + bookmarkId));
        bookmarkRepository.delete(bookmark);
    }
}
