package bento.backend.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
public class UserLoginResponse {
    private String message;          // 로그인 성공 메시지
    private String accessToken;      // JWT 토큰
    private String tokenType;        // 토큰 타입, 일반적으로 "Bearer"
    private int expiresIn;           // 토큰의 유효 시간 (초 단위)

    // Optional: 정적 팩토리 메서드를 통해 반환 객체를 쉽게 생성
    public static UserLoginResponse of(String accessToken, int expiresIn) {
        return UserLoginResponse.builder()
                .message("Login successful")
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}
