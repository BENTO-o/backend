package bento.backend.service.user;

import bento.backend.constant.ErrorMessages;
import bento.backend.dto.request.UserPasswordUpdateRequest;
import bento.backend.dto.request.UserUpdateRequest;
import bento.backend.exception.BadRequestException;
import bento.backend.exception.ConflictException;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import bento.backend.repository.UserRepository;
import bento.backend.domain.User;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final PasswordService passwordService;

	public User getUserById(final Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new BadRequestException(ErrorMessages.USER_ID_NOT_FOUND_ERROR + userId));
	}

	public User findByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new BadRequestException(ErrorMessages.USER_ID_NOT_FOUND_ERROR + username));
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new BadRequestException(ErrorMessages.USER_EMAIL_NOT_FOUND_ERROR + email));
	}

	public boolean verifyUserPassword(Long userId, String rawPassword) {
		User user = getUserById(userId);
		return passwordService.verifyPassword(rawPassword, user.getPassword());
	}

	public User updateEmail(Long userId, String newEmail) {
		User user = getUserById(userId);
		if (userRepository.existsByEmail(newEmail)) {
			throw new ConflictException(ErrorMessages.DUPLICATE_EMAIL_ERROR);
		}
		else if (newEmail == null || newEmail.isEmpty()) {
			throw new ValidationException(ErrorMessages.USER_EMAIL_EMPTY_ERROR);
		}
		else if (newEmail.equals(user.getEmail())) {
			throw new BadRequestException(ErrorMessages.SAME_EMAIL_ERROR);
		}
		user.setEmail(newEmail);
		return userRepository.save(user);
	}

	public void deleteUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new BadRequestException(ErrorMessages.USER_ID_NOT_FOUND_ERROR + userId);
		}
		userRepository.deleteById(userId);
	}

	public User getCurrentUser(Long userId) {
		return getUserById(userId);
	}

	public User getAdminUser(Long userId) {
		User user = getUserById(userId);
		if (!user.getRole().toString().equals("ROLE_ADMIN")) {
			throw new BadRequestException(ErrorMessages.CREDENTIALS_INVALID_ERROR);
		}
		return user;
	}

	public User updateUser(Long userId, @Valid UserUpdateRequest request) {
		User user = getUserById(userId);
		if (userRepository.existsByUsername(request.getUsername()) && !request.getUsername().equals(user.getUsername())) {
			throw new ConflictException(String.format(ErrorMessages.DUPLICATE_USERNAME_ERROR, request.getUsername()));
		}
		user.setUsername(request.getUsername());
		return userRepository.save(user);
	}

	public void deactivateUser(Long userId) {
		User user = getUserById(userId);
		user.setActive(false);
		userRepository.save(user);
	}

	public void updatePassword(Long userId, @Valid UserPasswordUpdateRequest request) {
		User user = getUserById(userId);
		if (!verifyUserPassword(userId, request.getCurrentPassword())) {
			throw new BadRequestException(ErrorMessages.PASSWORD_INCORRECT_ERROR);
		}
		String newPassword = request.getNewPassword();
		String encryptedPassword = passwordService.encodePassword(newPassword);
		user.setPassword(encryptedPassword);
		userRepository.save(user);
	}

    public String getRoleByUserId(Long userId) {
		User user = getUserById(userId);
		return user.getRole().toString();
    }
}