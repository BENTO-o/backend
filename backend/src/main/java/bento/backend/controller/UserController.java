package bento.backend.controller;

import bento.backend.constant.SuccessMessages;
import bento.backend.dto.request.*;
import bento.backend.security.JwtTokenProvider;
import bento.backend.service.user.PasswordService;
import bento.backend.service.user.UserService;
import bento.backend.domain.User;
import bento.backend.dto.response.UserLoginResponse;
import bento.backend.dto.response.UserProfileResponse;
import bento.backend.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
	private final AuthService authService; // 로그인, 로그아웃 관련 로직
	private final UserService userService;
	private final PasswordService passwordService;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping("/register")
	public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserRegistrationRequest request) {
		authService.registerUser(request);
		Map<String, String> response = Map.of("message", SuccessMessages.USER_REGISTERED);
		return ResponseEntity.status(201).body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest loginRequest) {
		UserLoginResponse response = authService.login(loginRequest);
		return ResponseEntity.status(200).body(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(@RequestBody RefreshTokenRequest request) {
		String refreshToken = request.getRefreshToken();
		authService.logout(refreshToken);
		Map<String, String> response = Map.of("message", SuccessMessages.USER_LOGGED_OUT);
		return ResponseEntity.status(200).body(response);
	}

	@PostMapping("/logout-all")
	public ResponseEntity<Map<String, String>> logoutAllDevices(@RequestHeader("Authorization") String accessToken) {
		Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);
		authService.logoutAllDevices(userId);
		return ResponseEntity.ok(Map.of("message", "Logged out from all devices"));
	}

	@PostMapping("/refresh")
	public ResponseEntity<UserLoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
		String refreshToken = request.getRefreshToken();
		String newAccessToken = authService.refreshAccessToken(refreshToken);
		UserLoginResponse response = UserLoginResponse.of(newAccessToken, refreshToken, jwtTokenProvider.getAccessTokenExpirationTime(), SuccessMessages.TOKEN_REFRESHED);
		return ResponseEntity.status(200).body(response);
	}

	@PostMapping("/request-password-reset")
	public ResponseEntity<Map<String, String>> requestPasswordReset(@Valid @RequestBody UserPasswordResetRequest request) {
		passwordService.requestPasswordReset(request);
		Map<String, String> response = Map.of("message", SuccessMessages.PASSWORD_RESET_REQUEST);
		return ResponseEntity.status(200).body(response);
	}

	@PostMapping("/reset-password")
	public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody UserPasswordResetExecutionRequest request) {
		passwordService.resetPassword(request);
		Map<String, String> response = Map.of("message", SuccessMessages.PASSWORD_RESET);
		return ResponseEntity.status(200).body(response);
	}

	@GetMapping("/reset-password/verify")
	public ResponseEntity<Map<String, String>> verifyResetPasswordToken(@RequestParam String token) {
		passwordService.verifyResetPasswordToken(token);
		Map<String, String> response = Map.of("message", SuccessMessages.PASSWORD_RESET_TOKEN_VERIFIED);
		return ResponseEntity.status(200).body(response);
	}

	@GetMapping("/me")
	public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal Long userId) {
		User user = userService.getUserById(userId); // Fetch the user from the database
		UserProfileResponse response = new UserProfileResponse(
				user.getUserId(),
				user.getUsername(),
				user.getEmail(),
				user.getRole().toString(),
				user.getOauthProviderId()
		);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/me")
	public ResponseEntity<UserProfileResponse> updateUser(@AuthenticationPrincipal Long userId, @Valid @RequestBody UserUpdateRequest request) {
		User user = userService.getUserById(userId);
		User updatedUser = userService.updateUser(user.getUserId(), request);
		UserProfileResponse currentUser = new UserProfileResponse(
				updatedUser.getUserId(),
				updatedUser.getUsername(),
				updatedUser.getEmail(),
				updatedUser.getRole().toString(),
				updatedUser.getOauthProviderId()
		);
		return ResponseEntity.ok(currentUser);
	}

	@DeleteMapping("/me")
	public ResponseEntity<Map<String, String>> deleteUser(
			@AuthenticationPrincipal Long userId,
			@RequestHeader("Authorization") String token) {

		// 계정 비활성화
		userService.deactivateUser(userId);

		Map<String, String> response = Map.of("message", SuccessMessages.USER_DEACTIVATED);
		return ResponseEntity.status(200).body(response);
	}


	@PutMapping("/me/password")
	public ResponseEntity<Map<String, String>> updatePassword(
			@AuthenticationPrincipal Long userId,
			@RequestHeader("Authorization") String token,
			@Valid @RequestBody UserPasswordUpdateRequest request) {
		passwordService.updatePassword(userId, request); // 비밀번호 업데이트

		Map<String, String> response = Map.of("message", SuccessMessages.PASSWORD_UPDATED);
		return ResponseEntity.status(200).body(response);
	}

}

