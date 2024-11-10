package bento.backend.controller;

import bento.backend.constant.SuccessMessages;
import bento.backend.dto.request.UserUpdateRequest;
import bento.backend.service.user.UserService;
import bento.backend.domain.User;
import bento.backend.dto.request.UserLoginRequest;
import bento.backend.dto.request.UserRegistrationRequest;
import bento.backend.dto.response.UserLoginResponse;
import bento.backend.dto.response.UserProfileResponse;
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
	private final UserService userService;

	@PostMapping("/login")
	public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest loginRequest) {
		UserLoginResponse response = authService.login(loginRequest);
		return ResponseEntity.status(200).body(response);
	}

	@PostMapping("/register")
	public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserRegistrationRequest request) {
		authService.registerUser(request);
		Map<String, String> response = Map.of("message", SuccessMessages.USER_REGISTERED);
		return ResponseEntity.status(201).body(response);
	}

	@GetMapping("/me")
	public ResponseEntity<UserProfileResponse> getCurrentUser(@RequestHeader("Authorization") String token) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));
		UserProfileResponse currentUser = new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().toString(),
                user.getOauthProviderId()
        );
		return ResponseEntity.status(200).body(currentUser);
	}

	@PatchMapping("/me")
	public ResponseEntity<User> updateUser(@RequestHeader("Authorization") String token, @Valid @RequestBody UserUpdateRequest request) {
		User user = authService.getUserFromToken(token.replace("Bearer ", ""));
		User updatedUser = userService.updateUser(user.getUserId(), request);
		return ResponseEntity.ok(updatedUser);
	}
}

