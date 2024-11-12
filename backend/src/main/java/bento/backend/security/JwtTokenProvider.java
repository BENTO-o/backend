package bento.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    @Value("${jwt.expiration-time}")
    private long expirationTime; // Configure in application.properties (in milliseconds)

    private final SecretKey secretKey;
    private final JwtParser jwtParser;
    private final JwtBuilder jwtBuilder;

    public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parserBuilder().setSigningKey(this.secretKey).build();
        this.jwtBuilder = Jwts.builder().signWith(this.secretKey, SignatureAlgorithm.HS512);
    }

    // User ID is used as the subject, with additional claims for roles/scopes
    public String generateToken(Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // Include roles or other claims as necessary

        return jwtBuilder
                .setClaims(claims)
                .setSubject(String.valueOf(userId)) // User ID as the subject
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = jwtParser
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public String getRoleFromToken(String token) {
        Claims claims = jwtParser
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

    public int getExpirationTime() {
        return (int) expirationTime / 1000;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
