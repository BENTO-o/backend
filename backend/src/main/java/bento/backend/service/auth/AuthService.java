package bento.backend.service.auth;

import bento.backend.constant.ErrorMessages;
import bento.backend.domain.Role;
import bento.backend.domain.User;
import bento.backend.dto.request.UserLoginRequest;
import bento.backend.dto.request.UserRegistrationRequest;
import bento.backend.dto.response.UserLoginResponse;
import bento.backend.exception.ConflictException;
import bento.backend.exception.UnauthorizedException;
import bento.backend.repository.UserRepository;
import bento.backend.security.JwtTokenProvider;
import bento.backend.service.user.PasswordService;
import bento.backend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
//    Authorization과 Authentication을 처리하는 서비스입니다.
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordService passwordService;

//    Authentication
    public UserLoginResponse login (UserLoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        User user = userService.findByEmail(email);
        if (!passwordService.verifyUserPassword(user.getUserId(), password)) {
            throw new UnauthorizedException(ErrorMessages.OAUTH_EMAIL_REQUIRED_ERROR);
        }
        String token = jwtTokenProvider.generateToken(user.getUserId(), String.valueOf(user.getRole()));
        int expiresIn = jwtTokenProvider.getExpirationTime();
        return UserLoginResponse.of(token, expiresIn);
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
        User user = new User(username, encryptedPassword, email, Role.ROLE_USER);
        return userRepository.save(user);
    }

//    Authorization
    public User getUserFromToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException(ErrorMessages.TOKEN_VALIDATION_ERROR);
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userService.findByUserId(userId);
        return user;
    }

    public boolean isAdminUser(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException(ErrorMessages.TOKEN_VALIDATION_ERROR);
        }
        String role = jwtTokenProvider.getRoleFromToken(token);
        return role.equals(Role.ROLE_ADMIN.toString());
    }
}
