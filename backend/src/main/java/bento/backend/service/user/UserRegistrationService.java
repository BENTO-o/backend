package bento.backend.service.user;

import bento.backend.constant.ErrorMessages;
import bento.backend.domain.User;
import bento.backend.dto.request.UserRegistrationRequest;
import bento.backend.exception.ConflictException;
import bento.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(UserRegistrationRequest request) {
        return registerUser(request.getUsername(), request.getEmail(), request.getPassword());
    }

    public User registerUser(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException(ErrorMessages.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(ErrorMessages.EMAIL_ALREADY_EXISTS);
        }
        String encryptedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username, encryptedPassword, email);
        return userRepository.save(user);
    }
}
