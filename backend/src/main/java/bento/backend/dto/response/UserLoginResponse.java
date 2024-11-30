package bento.backend.dto.response;

import bento.backend.constant.SuccessMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
public class UserLoginResponse {
    private String message;          // 로그인 성공 메시지
    private String accessToken;      // JWT 토큰
    private String refreshToken;     // JWT 리프레시 토큰
    private String tokenType;        // 토큰 타입, 일반적으로 "Bearer"
    private int expiresIn;           // 토큰의 유효 시간 (초 단위). 클라이언트에서 JWT 디코딩이 가능하다면 필요 없음

    // Optional: 정적 팩토리 메서드를 통해 반환 객체를 쉽게 생성
    public static UserLoginResponse of(String accessToken, String refreshToken, int expiresIn) {
        return UserLoginResponse.builder()
                .message(SuccessMessages.USER_LOGGED_IN)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }

    public static UserLoginResponse of(String accessToken, String refreshToken, int expiresIn, String message) {
        return UserLoginResponse.builder()
                .message(message)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}
