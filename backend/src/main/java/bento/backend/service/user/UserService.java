package bento.backend.service.user;

import bento.backend.constant.ErrorMessages;
import bento.backend.exception.BadRequestException;
import bento.backend.exception.ConflictException;
import bento.backend.exception.NotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import bento.backend.repository.UserRepository;
import bento.backend.domain.User;

@Service
@RequiredArgsConstructor
public class UserService { // 기존의 사용자 조회, 업데이트, 삭제 등의 일반적인 CRUD 작업을 처리합니다.
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public User findByUserId(final Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException(ErrorMessages.USER_ID_NOT_FOUND_ERROR + userId));
	}

	public User findByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new NotFoundException(ErrorMessages.USER_ID_NOT_FOUND_ERROR + username));
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new NotFoundException(ErrorMessages.USER_EMAIL_NOT_FOUND_ERROR + email));
	}

	public User updateEmail(Long userId, String newEmail) {
		User user = findByUserId(userId);
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
			throw new NotFoundException(ErrorMessages.USER_ID_NOT_FOUND_ERROR + userId);
		}
		userRepository.deleteById(userId);
	}

	public User getCurrentUser(Long userId) {
		return findByUserId(userId);
	}
}
