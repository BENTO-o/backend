package bento.backend.service.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklistService {
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>(); // TODO: use Redis for scalability

    public void blacklistToken(String token, long expirationTime) {
        blacklist.put(token, System.currentTimeMillis() + expirationTime);
    }

    public boolean isTokenBlacklisted(String token) {
        Long expiryTime = blacklist.get(token);
        if (expiryTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > expiryTime) {
            blacklist.remove(token); // Clean up expired tokens
            return false;
        }
        return true;
    }
}

