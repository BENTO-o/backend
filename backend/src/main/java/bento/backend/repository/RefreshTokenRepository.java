package bento.backend.repository;

import bento.backend.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    List<RefreshToken> findByUserId(Long userId);
    void deleteByRefreshToken(String refreshToken);
    void deleteByUserId(Long userId);

    void deleteAllByExpiresAtBefore(Timestamp timestamp);
}

