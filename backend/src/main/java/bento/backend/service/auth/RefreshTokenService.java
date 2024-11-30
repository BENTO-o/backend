package bento.backend.service.auth;

import bento.backend.domain.RefreshToken;
import bento.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    // Refresh Token 생성 및 저장
    public RefreshToken createRefreshToken(Long userId, String token, long expirationTime) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setRefreshToken(token);
        refreshToken.setExpiresAt(new Timestamp(System.currentTimeMillis() + expirationTime));
        return refreshTokenRepository.save(refreshToken);
    }

    // Refresh Token 검증
    public boolean validateRefreshToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByRefreshToken(token);
        return refreshToken.map(value -> value.getExpiresAt().after(new Date())).orElse(false);
        // 만료 시간 확인
    }

    @Transactional
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByRefreshToken(token);
    }

    // 사용자별 Refresh Token 삭제
    public void deleteRefreshTokensByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Scheduled(fixedRate = 86400000) // 매일 실행 (24시간 = 86400000ms)
    @Transactional
    public void cleanUpExpiredTokens() {
        refreshTokenRepository.deleteAllByExpiresAtBefore(new Timestamp(System.currentTimeMillis()));
        System.out.println("Expired Refresh Tokens cleaned up at: " + new Timestamp(System.currentTimeMillis()));
    }

    public void deleteRefreshTokenByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}

