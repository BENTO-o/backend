package bento.backend.service.user;

import bento.backend.constant.ErrorMessages;
import bento.backend.dto.request.UserPasswordUpdateRequest;
import bento.backend.dto.request.UserUpdateRequest;
import bento.backend.exception.BadRequestException;
import bento.backend.exception.ConflictException;
import bento.backend.service.auth.RefreshTokenService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import bento.backend.repository.UserRepository;
import bento.backend.domain.User;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
	private final RefreshTokenService refreshTokenService;
	private final UserRepository userRepository;

	public User getUserById(final Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new BadRequestException(ErrorMessages.USER_ID_NOT_FOUND_ERROR + userId));
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new BadRequestException(ErrorMessages.USER_EMAIL_NOT_FOUND_ERROR + email));
	}

	public User updateUser(Long userId, @Valid UserUpdateRequest request) {
		User user = getUserById(userId);
		if (userRepository.existsByUsername(request.getUsername()) && !request.getUsername().equals(user.getUsername())) {
			throw new ConflictException(String.format(ErrorMessages.DUPLICATE_USERNAME_ERROR, request.getUsername()));
		}
		user.setUsername(request.getUsername());
		return userRepository.save(user);
	}

	@Transactional
	public void deactivateUser(Long userId) {
		User user = getUserById(userId);
		user.setActive(false);
		refreshTokenService.deleteRefreshTokenByUserId(userId);
		userRepository.save(user);
	}

	public void deleteUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new BadRequestException(ErrorMessages.USER_ID_NOT_FOUND_ERROR + userId);
		}
		refreshTokenService.deleteRefreshTokenByUserId(userId);
		userRepository.deleteById(userId);
	}

    public String getRoleByUserId(Long userId) {
		User user = getUserById(userId);
		return user.getRole().toString();
    }

	public boolean isActive(Long userId) {
		User user = getUserById(userId);
		return user.isActive();
	}
}