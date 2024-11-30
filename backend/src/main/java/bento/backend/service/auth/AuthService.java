package bento.backend.service.auth;

import bento.backend.constant.ErrorMessages;
import bento.backend.constant.SuccessMessages;
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

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    //    Authorization과 Authentication을 처리하는 서비스입니다.
    private final UserService userService;
    private final PasswordService passwordService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    //    Authentication
    public UserLoginResponse login(UserLoginRequest loginRequest) {
        User user = userService.findByEmail(loginRequest.getEmail());
        if (!passwordService.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(ErrorMessages.CREDENTIALS_INVALID_ERROR);
        }

        if (!user.isActive()) {
            throw new UnauthorizedException(ErrorMessages.USER_NOT_ACTIVE_ERROR);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getRole().toString());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
        refreshTokenService.createRefreshToken(user.getUserId(), refreshToken, jwtTokenProvider.getRefreshTokenExpirationTime());
        return UserLoginResponse.of(accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpirationTime());
    }

    public void logout(String refreshToken) {
        refreshTokenService.deleteRefreshToken(refreshToken);
    }

    public void logoutAllDevices(Long userId) {
        refreshTokenService.deleteRefreshTokensByUserId(userId);
    }

    public Map<String, String> registerUser(UserRegistrationRequest request) {
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
        userRepository.save(user);
        return Map.of("message", SuccessMessages.USER_REGISTERED);
    }


    //    Authorization
    public String refreshAccessToken(String refreshToken) {
        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String role = userService.getRoleByUserId(userId);
        return jwtTokenProvider.generateAccessToken(userId, role);
    }
}
