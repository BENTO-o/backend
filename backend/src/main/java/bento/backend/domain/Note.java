package bento.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "note")
public class Note {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "note_id")
	private Long noteId;

	@Column(name = "title")
	private String title;

	// JSON array of script objects. This is a response from the Naver Speech-to-Text API.
	// This is a temporary solution. We will change this to a list of script objects.
	@Column(name = "content", columnDefinition = "json")
	private String content;

	@CreatedDate
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "folder", nullable = false)
	private String folder;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "audio_id", nullable = false)
	private Audio audio;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Bookmark> bookmarks;

	@OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Memo> memos;

	@OneToOne(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
	private Summary summary;

	public String getFormattedDateTime(LocalDateTime dateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return dateTime.format(formatter);
	}

	public void update(String title, String folder) {
		if (title != null) this.title = title;
		if (folder != null) this.folder = folder;
	}
}
