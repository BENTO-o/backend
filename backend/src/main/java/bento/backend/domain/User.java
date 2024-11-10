package bento.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user")
public class User implements UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "username", nullable = false, unique = true)
	private String username;

	@Setter
    @Column(name = "password", nullable = true)
	private String password;

	@Setter
    @Email(message = "Email should be valid")
	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "oauth_provider")
	private OauthProvider oauthProvider;

	@Column(name = "oauth_provider_id", unique = true)
	private String oauthProviderId; // 로그인 한 유저의 고유 ID

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private Role role;

	@Builder
	public User(String username, String password, String email, Role role) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.role = (role != null) ? role : Role.ROLE_USER;
	}

	@Builder
	public User(String email, OauthProvider oauthProvider, String oauthProviderId, Role role) {
		this.username = oauthProvider.name() + "_" + oauthProviderId;
		this.email = email;
		this.oauthProvider = oauthProvider;
		this.oauthProviderId = oauthProviderId;
		this.role = (role != null) ? role : Role.ROLE_USER;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}