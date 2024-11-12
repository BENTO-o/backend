package bento.backend.controller;

import bento.backend.service.user.UserService;
import bento.backend.dto.request.UserLoginRequest;
import bento.backend.dto.request.UserRegistrationRequest;
import bento.backend.dto.response.UserLoginResponse;
import bento.backend.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
	private final AuthService authService; // 로그인, 로그아웃 관련 로직

	@PostMapping("/login")
	public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest loginRequest) {
		UserLoginResponse response = authService.login(loginRequest);
		return ResponseEntity.status(200).body(response);
	}

	@PostMapping("/register")
	public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserRegistrationRequest request) {
		Map<String, String> response = authService.registerUser(request);
		return ResponseEntity.status(201).body(response);
	}
}
