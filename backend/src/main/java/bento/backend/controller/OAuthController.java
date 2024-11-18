package bento.backend.controller;

import bento.backend.domain.User;
import bento.backend.dto.response.UserLoginResponse;
import bento.backend.security.CustomOAuth2User;
import bento.backend.security.JwtTokenProvider;
import bento.backend.service.auth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OAuthController {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/login")
    public ResponseEntity<Void> initiateLogin() {
        // 사용자를 OAuth2 제공자의 로그인 페이지로 리다이렉트
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .header("Location", "/oauth2/authorization/naver") // Naver 인증 요청
                .build();
    }

    // OAuth2 인증 성공 후 JWT 발급하는 엔드포인트
    @GetMapping("/login/success")
    public ResponseEntity<UserLoginResponse> loginSuccess(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        UserLoginResponse response = customOAuth2UserService.login(customOAuth2User);
        return ResponseEntity.status(200).body(response);
    }

    // OAuth2 인증 실패 처리 (선택적)
    @GetMapping("/login/failure")
    public ResponseEntity<String> loginFailure() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Login failed. Please try again."); // 실패 메시지 반환
    }
}
