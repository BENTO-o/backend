package bento.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 공통 응답 형식 생성
    private ResponseEntity<Object> buildResponseEntity(String error, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
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
        return buildResponseEntity(status.getReasonPhrase(), ex.getMessage(), status);
    }

    // 예상하지 못한 일반적인 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
//        return buildResponseEntity("Internal Server Error", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        HttpStatus status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        return buildResponseEntity("Undefined Error", ex.getMessage(), status);
    }

    // @ResponseStatus 애노테이션에서 상태 코드 가져오기 (없을 경우 INTERNAL_SERVER_ERROR 반환)
    @ResponseStatus
    private HttpStatus getResponseStatus(Exception ex) {
        ResponseStatus statusAnnotation = ex.getClass().getAnnotation(ResponseStatus.class);
        return (statusAnnotation != null) ? statusAnnotation.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
