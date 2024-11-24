package bento.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "audio")
public class Audio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audio_id")
    private Long audioId;

//    @Column(name = "title", nullable = false)
//    private String title;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "duration", nullable = false)
    @Pattern(
            regexp = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$",
            message = "Duration must be in the format of HH:mm:ss"
    )
    private String duration;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AudioStatus status;

    @Column(name = "language", nullable = false)
    private String language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(mappedBy = "audio")
    private Note note;

//    public String getFormattedDateTime(LocalDateTime dateTime) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        return dateTime.format(formatter);
//    }
}
