package bento.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 공통 응답 형식 생성
    private ResponseEntity<Object> buildResponseEntity(String error, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        response.put("timestamp", LocalDateTime.now().format(formatter));
        response.put("error", error);
        response.put("message", message);
        response.put("status", status.value());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler({
            BadRequestException.class,
            ConflictException.class,
            ResourceNotFoundException.class,
            UnauthorizedException.class,
            ValidationException.class,
    })
    public ResponseEntity<Object> handleCustomExceptions(Exception ex) {
        HttpStatus status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        logger.warn("Custom Exception Occurred: {} - {}", status, ex.getMessage());
        return buildResponseEntity(status.getReasonPhrase(), ex.getMessage(), status);
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<Object> handleAccountStatusException(AccountStatusException ex) {
        logger.error("AccountStatusException: {}", ex.getMessage());
        return buildResponseEntity("Account Disabled", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    // 예상하지 못한 일반적인 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex, HttpServletRequest request) {
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        HttpStatus status = (responseStatus != null) ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;

        // 예외 스택 트레이스 로그 기록
        logger.error("Unhandled Exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildResponseEntity("Internal Server Error", ex.getMessage(), status);
    }

}
