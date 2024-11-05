package bento.backend.constant;

public class ErrorMessages {
    public static final String USER_NOT_FOUND = "User not found with id: ";
    public static final String EMAIL_NOT_FOUND = "User not found with email: ";
    public static final String USERNAME_ALREADY_EXISTS = "The username '%s' is already taken. Please choose a different username.";
    public static final String EMAIL_ALREADY_EXISTS = "An account with the email '%s' already exists. Please use a different email address.";
    public static final String TOKEN_MISSING_OR_INVALID = "The token is missing or invalid in the OAuth login response. Please provide a valid token.";
    public static final String INVALID_TOKEN = "Error occurred while fetching token from OAuth provider.";
    public static final String OAUTH_EMAIL_REQUIRED = "Email is required to authenticate with OAuth provider.";
    // 필요한 다른 에러 메시지들을 추가
}
