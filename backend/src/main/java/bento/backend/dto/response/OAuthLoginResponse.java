package bento.backend.dto.response;

import bento.backend.constant.ErrorMessages;
import bento.backend.exception.UnauthorizedException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
public class OAuthLoginResponse {
    private String email;
    private String token;

    public OAuthLoginResponse(String token) {
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedException(ErrorMessages.OAUTH_TOKEN_INVALID_ERROR);
        }
        this.token = token;
    }

    public OAuthLoginResponse(String email, String token) {
        this(token);
        this.email = email;
    }
}
