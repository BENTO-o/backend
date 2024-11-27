package bento.backend.constant;

public class ErrorMessages {
    public static final String USER_ID_NOT_FOUND_ERROR = "User with the specified ID could not be found : ";
    public static final String USER_EMAIL_NOT_FOUND_ERROR = "No user found associated with the provided email address : ";
    public static final String USER_EMAIL_EMPTY_ERROR = "Email address cannot be empty.";
    public static final String DUPLICATE_USERNAME_ERROR = "The username '%s' is already in use. Please select a different username.";
    public static final String DUPLICATE_EMAIL_ERROR = "An account with the email '%s' already exists. Please choose another email.";
    public static final String SAME_EMAIL_ERROR = "The new email address is the same as the current email address. Please provide a different email address.";
    public static final String OAUTH_RESPONSE_ERROR = "OAuth login failed: Invalid response received from OAuth provider.";
    public static final String TOKEN_VALIDATION_ERROR = "The provided token is invalid. Please check and provide a valid token.";
    public static final String CREDENTIALS_INVALID_ERROR = "Authentication failed: Invalid credentials provided.";
    public static final String PASSWORD_INCORRECT_ERROR = "The password provided is incorrect. Please check and try again.";
    public static final String NOTE_ID_NOT_FOUND_ERROR = "The note with the specified ID could not be found : ";
    public static final String UNAUTHORIZED_ERROR = "You are not authorized to perform this action. Please log in and try again.";
    public static final String BOOKMARK_ID_NOT_FOUND_ERROR = "The bookmark with the specified ID could not be found : ";
    public static final String BOOKMARK_TIMESTAMP_ERROR = "The bookmark timestamp is invalid. Please provide a valid timestamp.";
    public static final String BOOKMARK_ALREADY_EXISTS_ERROR = "A bookmark with the same timestamp already exists for this note.";
    public static final String MEMO_TIMESTAMP_ERROR = "The memo timestamp is invalid. Please provide a valid timestamp.";
    public static final String MEMO_ALREADY_EXISTS_ERROR = "A memo with the same timestamp already exists for this note.";
    public static final String MEMO_ID_NOT_FOUND_ERROR = "The memo with the specified ID could not be found : ";
    public static final String INVALID_JSON_FORMAT = "The provided JSON data is invalid. Please check bookmarks and memos and try again.";
    // 필요한 다른 에러 메시지들을 추가
}
