package bento.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String email;
    private String oauthProvider;
    private String oauthProviderId;
}