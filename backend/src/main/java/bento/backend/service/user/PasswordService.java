package bento.backend.service.user;

import bento.backend.constant.ErrorMessages;
import bento.backend.domain.User;
import bento.backend.dto.request.UserPasswordResetExecutionRequest;
import bento.backend.dto.request.UserPasswordResetRequest;
import bento.backend.dto.request.UserPasswordUpdateRequest;
import bento.backend.exception.BadRequestException;
import bento.backend.exception.UnauthorizedException;
import bento.backend.repository.UserRepository;
import bento.backend.security.JwtTokenProvider;
import bento.backend.service.auth.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public void updatePassword(Long userId, @Valid UserPasswordUpdateRequest request) {
        User user = userService.getUserById(userId);
        if (!verifyPassword(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException(ErrorMessages.PASSWORD_INCORRECT_ERROR);
        }
        String newPassword = request.getNewPassword();
        String encryptedPassword = encodePassword(newPassword);
        user.setPassword(encryptedPassword);
        userRepository.save(user);
    }

    public void resetPasswordRequest(@Valid UserPasswordResetRequest request) {
        String email = request.getEmail();
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new UnauthorizedException(ErrorMessages.USER_EMAIL_NOT_FOUND_ERROR + email);
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
        String email = jwtTokenProvider.getSubjectFromToken(token);
        User user = userService.findByEmail(email);

        String encryptedPassword = encodePassword(password);
        user.setPassword(encryptedPassword);
        userRepository.save(user);

        refreshTokenService.deleteRefreshTokenByUserId(user.getUserId());
    }

    public void verifyResetPasswordToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException(ErrorMessages.TOKEN_VALIDATION_ERROR);
        }
    }
}