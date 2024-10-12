package bento.backend.service;

import bento.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	public String getTest() {
		// User user = userRepository.findById(1L)
		// 	.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
		return "test";
	}
}
