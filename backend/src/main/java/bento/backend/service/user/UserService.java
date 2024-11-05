package bento.backend.service.user;

import bento.backend.constant.ErrorMessages;
import bento.backend.domain.User;
import bento.backend.exception.ConflictException;
import bento.backend.exception.ResourceNotFoundException;
import bento.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService { // 기존의 사용자 조회, 업데이트, 삭제 등의 일반적인 CRUD 작업을 처리합니다.
	private final UserRepository userRepository;

	public User findByUserId(final Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND + userId));
	}

	public User findByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND + username));
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.EMAIL_NOT_FOUND + email));
	}

	public User updateEmail(Long userId, String newEmail) {
		User user = findByUserId(userId);
		if (userRepository.existsByEmail(newEmail)) {
			throw new ConflictException(ErrorMessages.EMAIL_ALREADY_EXISTS);
		}
		else if (newEmail == null || newEmail.isEmpty()) {
			throw new IllegalArgumentException("Email cannot be empty.");
		}
		else if (newEmail.equals(user.getEmail())) {
			throw new IllegalArgumentException("New email must be different from the current email.");
		}
		user.setEmail(newEmail);
		return userRepository.save(user);
	}

	public void deleteUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND + userId);
		}
		userRepository.deleteById(userId);
	}

	public User getCurrentUser(Long userId) {
		return findByUserId(userId);
	}
}
