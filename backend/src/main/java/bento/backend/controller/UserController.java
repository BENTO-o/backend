package bento.backend.controller;

import bento.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
	private final UserService userService;

	@GetMapping("/test")
	public ResponseEntity<String> getTest() {
		return ResponseEntity.status(HttpStatus.OK)
			.body(userService.getTest());
	}
}
