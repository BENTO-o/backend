package bento.backend.controller;

import bento.backend.domain.User;
import bento.backend.dto.request.UserLoginRequest;
import bento.backend.dto.request.UserRegistrationRequest;
import bento.backend.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
	private final AuthService authService; // 로그인, 로그아웃 관련 로직

	@PostMapping("/login")
	public ResponseEntity<String> login(@Valid @RequestBody UserLoginRequest loginRequest) {
		String token = authService.login(loginRequest);
		return ResponseEntity.status(200).body(token);
	}
	// 로그아웃 로직은 클라이언트 측에서 토큰을 폐기하는 방식이 일반적입니다. 서버 측에서는 토큰을 폐기하는 작업을 수행하지 않습니다.

	@PostMapping("/register")
	public ResponseEntity<User> register(@Valid @RequestBody UserRegistrationRequest request) {
		User user = authService.registerUser(request);
		return ResponseEntity.status(201).body(user);
	}
}
