package bento.backend.service.auth;

import bento.backend.constant.ErrorMessages;
import bento.backend.domain.User;
import bento.backend.dto.request.UserLoginRequest;
import bento.backend.dto.request.UserRegistrationRequest;
import bento.backend.exception.ConflictException;
import bento.backend.exception.UnauthorizedException;
import bento.backend.repository.UserRepository;
import bento.backend.security.JwtTokenProvider;
import bento.backend.service.user.PasswordService;
import bento.backend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public User getAuthenticatedUser(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException(ErrorMessages.TOKEN_VALIDATION_ERROR);
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        return userService.findByUsername(username);
    }

    public String login (UserLoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        User user = userService.findByUsername(username);
        if (!passwordService.verifyUserPassword(user.getUserId(), password)) {
            throw new UnauthorizedException(ErrorMessages.OAUTH_EMAIL_REQUIRED_ERROR);
        }
        return jwtTokenProvider.generateToken(username);
    }

    public User registerUser(UserRegistrationRequest request) {
        String username = request.getUsername();
        String email = request.getEmail();
        String rawPassword = request.getPassword();

        if (userRepository.existsByUsername(username)) {
            throw new ConflictException(ErrorMessages.DUPLICATE_USERNAME_ERROR);
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(ErrorMessages.DUPLICATE_EMAIL_ERROR);
        }
        String encryptedPassword = passwordService.encodePassword(rawPassword);
        User user = new User(username, encryptedPassword, email);
        return userRepository.save(user);
    }
}
