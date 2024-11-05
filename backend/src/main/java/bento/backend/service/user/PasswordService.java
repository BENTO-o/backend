package bento.backend.service.user;

import bento.backend.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public boolean verifyUserPassword(Long userId, String rawPassword) {
        User user = userService.findByUserId(userId);
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}
