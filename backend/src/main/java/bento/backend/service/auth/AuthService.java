package bento.backend.service.auth;

import bento.backend.domain.User;
import bento.backend.dto.request.UserLoginRequest;
import bento.backend.security.JwtTokenProvider;
import bento.backend.service.user.PasswordService;
import bento.backend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final PasswordService passwordService;

    public User getAuthenticatedUser(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid token.");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        return userService.findByUsername(username);
    }

    public String login (UserLoginRequest loginRequest) {
        return login(loginRequest.getUsername(), loginRequest.getPassword());
    }

    public String login(String username, String password) {
        User user = userService.findByUsername(username);
        if (!passwordService.verifyUserPassword(user.getUserId(), password)) {
            throw new IllegalArgumentException("Invalid password.");
        }
        return jwtTokenProvider.generateToken(username);
    }
}
