package bento.backend.service.auth;

import bento.backend.constant.ErrorMessages;
import bento.backend.constant.SuccessMessages;
import bento.backend.domain.Role;
import bento.backend.domain.User;
import bento.backend.dto.request.UserLoginRequest;
import bento.backend.dto.request.UserPasswordResetExecutionRequest;
import bento.backend.dto.request.UserPasswordResetRequest;
import bento.backend.dto.request.UserRegistrationRequest;
import bento.backend.dto.response.UserLoginResponse;
import bento.backend.exception.ConflictException;
import bento.backend.exception.ResourceNotFoundException;
import bento.backend.exception.UnauthorizedException;
import bento.backend.repository.BookmarkRepository;
import bento.backend.repository.UserRepository;
import bento.backend.security.JwtTokenProvider;
import bento.backend.service.user.PasswordService;
import bento.backend.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
//    Authorization과 Authentication을 처리하는 서비스입니다.
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final PasswordService passwordService;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

    //    Authentication
    public UserLoginResponse login (UserLoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        User user = userService.findByEmail(email);
        if (!userService.verifyUserPassword(user.getUserId(), password)) {
            throw new UnauthorizedException(ErrorMessages.CREDENTIALS_INVALID_ERROR);
        }
        String token = jwtTokenProvider.generateToken(user.getUserId(), String.valueOf(user.getRole()));
        return UserLoginResponse.of(token, jwtTokenProvider.getExpirationTime());
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
    public User getUserFromToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException(ErrorMessages.TOKEN_VALIDATION_ERROR);
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        return userService.findByUserId(userId);
    }

    public boolean isAdminUser(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException(ErrorMessages.TOKEN_VALIDATION_ERROR);
        }
        String role = jwtTokenProvider.getRoleFromToken(token);
        return role.equals(Role.ROLE_ADMIN.toString());
    }

    public void requestPasswordReset(@Valid UserPasswordResetRequest request) {
        String email = request.getEmail();
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException(ErrorMessages.USER_EMAIL_NOT_FOUND_ERROR + email);
        }
        // 비밀번호 재설정 토큰 생성
        String token = jwtTokenProvider.generateResetToken(email);

        // 재설정 링크 생성
        String resetLink = "https://bento-o.site/reset-password?token=" + token;

        // 이메일 전송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Verification");
        message.setText("We received a request to reset your password. Please verify your identity by clicking the link below:\n\n" +
                resetLink + "\n\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "Thanks,\nTeam Bento");
        mailSender.send(message);
    }

    public void resetPassword(@Valid UserPasswordResetExecutionRequest request) {
        String token = request.getToken();
        String password = request.getPassword();
        String email = jwtTokenProvider.getEmailFromResetToken(token);
        User user = userService.findByEmail(email);
        String encryptedPassword = passwordService.encodePassword(password);
        user.setPassword(encryptedPassword);
        userRepository.save(user);
    }

    public void verifyResetPasswordToken(String token) {
        if (!jwtTokenProvider.validateResetToken(token)) {
            throw new UnauthorizedException(ErrorMessages.TOKEN_VALIDATION_ERROR);
        }
    }

    public boolean canDeleteBookmark(String token, Long bookmarkId) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException(ErrorMessages.TOKEN_VALIDATION_ERROR);
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userService.findByUserId(userId);
        boolean isAdmin = user.getRole().equals(Role.ROLE_ADMIN);
        return bookmarkRepository.existsByIdAndUserOrAdmin(bookmarkId, userId, isAdmin);
    }
}
