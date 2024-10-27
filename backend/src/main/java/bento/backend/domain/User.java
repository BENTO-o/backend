package bento.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@Table(name = "user")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	@NotBlank(message = "Username is mandatory")
	@Size(min = 4, max = 20, message = "Username should be between 4 and 20 characters")
	@Column(name = "username", nullable = false)
	private String username;

	@NotBlank(message = "Password is mandatory")
	@Size(min = 8, max = 20, message = "Password should be between 8 and 20 characters")
	@Column(name = "password", nullable = false)
	private String password;

	@Email(message = "Email should be valid")
	@Column(name = "email", nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "oauth_provider")
	private OauthProvider oauthProvider;

	@Column(name="oauth_provider_id")
	private String oauthProviderId;

	public void changePassword(String password) {
		this.password = hashPassword(password);
	}

	private String hashPassword(String rawPassword) {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return passwordEncoder.encode(rawPassword);
	}

	public boolean checkPassword(String rawPassword, String hashedPassword) {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		return passwordEncoder.matches(rawPassword, hashedPassword);
	}

}
