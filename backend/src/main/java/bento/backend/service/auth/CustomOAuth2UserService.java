package bento.backend.service.auth;

import bento.backend.constant.ErrorMessages;
import bento.backend.domain.OauthProvider;
import bento.backend.domain.User;
import bento.backend.exception.ValidationException;
import bento.backend.repository.UserRepository;
import bento.backend.security.CustomOAuth2User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Object response = oAuth2User.getAttributes().get("response");
        if (!(response instanceof Map)) {
            throw new ValidationException(ErrorMessages.OAUTH_RESPONSE_ERROR);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) response;

        System.out.println("attributes: " + attributes);

        // Extract necessary user information from the attributes
        String email = (String)attributes.get("email");
        String name = (String) attributes.get("name");
        OauthProvider oauthProvider = OauthProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
        String providerId = (String) attributes.get("id");

        if (email == null || name == null || providerId == null) {
            throw new ValidationException(ErrorMessages.OAUTH_RESPONSE_ERROR);
        }

        // Check if the user already exists in the database
        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            // Create a new user
            user = new User(name, email, oauthProvider, providerId, null);
            userRepository.save(user);
        }

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}