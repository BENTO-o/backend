package bento.backend.service.auth;

import bento.backend.constant.ErrorMessages;
import bento.backend.domain.OauthProvider;
import bento.backend.domain.User;
import bento.backend.dto.response.UserLoginResponse;
import bento.backend.exception.ValidationException;
import bento.backend.repository.UserRepository;
import bento.backend.security.CustomOAuth2User;
import bento.backend.security.JwtTokenProvider;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = extractAttributes(oAuth2User);
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("id");

        validateAttributes(email, name, providerId);

        OauthProvider oauthProvider = OauthProvider.valueOf(
                userRequest.getClientRegistration().getRegistrationId().toUpperCase()
        );

        User user = findOrCreateUser(email, name, oauthProvider, providerId);

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private Map<String, Object> extractAttributes(OAuth2User oAuth2User) {
        Object response = oAuth2User.getAttributes().get("response");
        if (!(response instanceof Map)) {
            throw new ValidationException(ErrorMessages.OAUTH_RESPONSE_ERROR);
        }
        return (Map<String, Object>) response;
    }

    private void validateAttributes(String email, String name, String providerId) {
        if (email == null || name == null || providerId == null) {
            throw new ValidationException(ErrorMessages.OAUTH_RESPONSE_ERROR);
        }
    }

    private User findOrCreateUser(String email, String name, OauthProvider provider, String providerId) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User(name, email, provider, providerId, null);
            return userRepository.save(newUser);
        });
    }

    public UserLoginResponse login(CustomOAuth2User customOAuth2User) {
        User user = customOAuth2User.getUser();
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), String.valueOf(user.getRole()));
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
        return UserLoginResponse.of(accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpirationTime());
    }
}