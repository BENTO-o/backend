package bento.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.access-expiration-time}")
    private long accessExpirationTime;
    @Value("${jwt.refresh-expiration-time}")
    private long refreshExpirationTime;

    private final SecretKey secretKey;
    private final JwtParser jwtParser;
    private final JwtBuilder jwtBuilder;

    public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parserBuilder().setSigningKey(this.secretKey).build();
        this.jwtBuilder = Jwts.builder().signWith(this.secretKey, SignatureAlgorithm.HS512);
    }

    //    Token generation
    public String generateAccessToken(Long userId, String role) {
        return jwtBuilder
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationTime))
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return jwtBuilder
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationTime))
                .compact();
    }

    public String generateResetToken(String email) {
        return jwtBuilder
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 300000)) // 5 minutes
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            return !claims.getExpiration().before(new Date(System.currentTimeMillis()));
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    public String getSubjectFromToken(String token) {
        return jwtParser.parseClaimsJws(token).getBody().getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getSubjectFromToken(token)); // Convert to Long
    }

    public String getClaimeFromToken(String token, String key) {
        return jwtParser.parseClaimsJws(token).getBody().get(key, String.class);
    }

    public int getAccessTokenExpirationTime() {
        return (int) accessExpirationTime / 1000; // Convert to seconds
    }

    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        String role = getClaimeFromToken(token, "role");
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    public long getRefreshTokenExpirationTime() {
        return refreshExpirationTime;
    }
}
