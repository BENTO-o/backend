package bento.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import bento.backend.repository.UserRepository;
import bento.backend.domain.User;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	public String getTest() {
		User user = userRepository.findById(1L)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
		return "test";
	}
}
