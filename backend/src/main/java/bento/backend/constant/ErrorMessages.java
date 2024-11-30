package bento.backend.constant;

public class ErrorMessages {
    public static final String USER_ID_NOT_FOUND_ERROR = "User with the specified ID could not be found : ";
    public static final String USER_EMAIL_NOT_FOUND_ERROR = "No user found associated with the provided email address : ";
    public static final String USER_EMAIL_EMPTY_ERROR = "Email address cannot be empty.";
    public static final String DUPLICATE_USERNAME_ERROR = "Username is already taken. Please choose a different username.";
    public static final String DUPLICATE_EMAIL_ERROR = "An account with the provided email address already exists. Please use a different email address.";
    public static final String SAME_EMAIL_ERROR = "The new email address is the same as the current email address. Please provide a different email address.";
    public static final String OAUTH_TOKEN_INVALID_ERROR = "OAuth login failed: Token is missing or invalid. Please ensure a valid token is provided.";
    public static final String OAUTH_EMAIL_REQUIRED_ERROR = "Email address is required to complete OAuth authentication.";
    public static final String OAUTH_RESPONSE_ERROR = "OAuth login failed: Invalid response received from OAuth provider.";
    public static final String TOKEN_VALIDATION_ERROR = "The provided token is invalid. Please check and provide a valid token.";
    public static final String CREDENTIALS_INVALID_ERROR = "Authentication failed: Invalid credentials provided.";
    public static final String PASSWORD_INCORRECT_ERROR = "The password provided is incorrect. Please check and try again.";
    public static final String USER_NOT_ACTIVE_ERROR = "The user account is not active. Please contact support for assistance.";
    // 필요한 다른 에러 메시지들을 추가
}
