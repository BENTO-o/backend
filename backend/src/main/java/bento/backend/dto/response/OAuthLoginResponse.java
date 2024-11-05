package bento.backend.dto.response;

import bento.backend.constant.ErrorMessages;
import bento.backend.exception.UnauthorizedException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OAuthLoginResponse {
    private String email;
    private String token;

    public OAuthLoginResponse(String token) {
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedException(ErrorMessages.TOKEN_MISSING_OR_INVALID);
        }
        this.token = token;
    }

    public OAuthLoginResponse(String email, String token) {
        this(token);
        this.email = email;
    }
}
