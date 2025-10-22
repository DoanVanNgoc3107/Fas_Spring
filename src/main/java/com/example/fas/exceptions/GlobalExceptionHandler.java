package com.example.fas.exceptions;

import com.example.fas.exceptions.auth.LoginFailedException;
import com.example.fas.exceptions.user.exists.IdentityCardExistsException;
import com.example.fas.exceptions.user.exists.PhoneNumberExistsException;
import com.example.fas.exceptions.user.exists.UsernameExistsException;
import com.example.fas.exceptions.user.invalid.IdentityCardInvalidException;
import com.example.fas.exceptions.user.invalid.PasswordInvalidException;
import com.example.fas.exceptions.user.invalid.PhoneNumberInvalidException;
import com.example.fas.exceptions.user.invalid.UserIDInvalidException;
import com.example.fas.exceptions.user.invalid.UserNotNullException;
import com.example.fas.exceptions.user.invalid.UsernameInvalidException;
import com.example.fas.exceptions.user.notFound.IdentityCardNotFoundException;
import com.example.fas.exceptions.user.notFound.PhoneNumberNotFoundException;
import com.example.fas.exceptions.user.notFound.UserIDNotFoundException;
import com.example.fas.exceptions.user.notFound.UsernameNotFoundException;

import org.apache.catalina.WebResource;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // private static final Logger logger =
    // LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // handle all uncaught exceptions
    @Getter
    @Setter
    public static class ErrorResponse {
        private int status; // HTTP status code (400, 404, 500...)
        private String timestamp; // Thời gian xảy ra lỗi
        private String error; // Loại lỗi (VALIDATION_ERROR, NOT_FOUND...)
        private String message; // Thông báo lỗi cho user
        private String path; // Endpoint nào bị lỗi
        private Map<String, Object> details; // Chi tiết bổ sung (optional)

        // Constructor mặc định - tự động set timestamp
        public ErrorResponse() {
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        // Constructor đầy đủ
        public ErrorResponse(int status, String error, String message, String path) {
            this(); // Gọi constructor mặc định để set timestamp
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = extractPath(path);
        }

        // Helper method: Lấy path từ WebRequest description
        private String extractPath(String requestDescription) {
            if (requestDescription != null && requestDescription.startsWith("uri=")) {
                return requestDescription.substring(4);
            }
            return requestDescription;
        }
    }

    /*
     * Exception handler for ...
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        // Tạo message dễ hiểu cho client
        String userMessage = "Invalid JSON format.";

        // Phân tích loại lỗi để đưa ra gợi ý cụ thể
        String originalMessage = ex.getMessage();
        if (originalMessage != null) {
            if (originalMessage.contains("Required request body is missing")) {
                userMessage = "Request body is missing.";
            } else if (originalMessage.contains("JSON parse error")) {
                userMessage = "Invalid JSON format.";
            } else if (originalMessage.contains("Cannot deserialize")) {
                userMessage = "Cannot deserialize JSON. Check field types.";
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getClass().getSimpleName(),
                userMessage,
                request.getDescription(false));

        // Thêm thông tin debug
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("original_error", originalMessage);
        debugInfo.put("suggestion", "Check JSON syntax and field types");
        debugInfo.put("common_causes", "Missing commas, unclosed brackets, incorrect field types");
        errorResponse.setDetails(debugInfo);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /*
     * Exception handler cho tất cả các lỗi không được xử lý
     */
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getClass().getSimpleName(),
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false));

        // Thêm thông tin debug
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("original_error", ex.getMessage());
        debugInfo.put("suggestion", "Contact support if the issue persists");
        errorResponse.setDetails(debugInfo);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /*
     * Exception handler for field exists
     */
    @ExceptionHandler({
            UsernameExistsException.class,
            PhoneNumberExistsException.class,
            IdentityCardExistsException.class
    })
    public ResponseEntity<ErrorResponse> handlerFieldExistsException(
            RuntimeException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /*
     * Exception handler for resource not found
     */
    @ExceptionHandler({
            UserIDNotFoundException.class,
            UsernameNotFoundException.class,
            PhoneNumberNotFoundException.class,
            IdentityCardNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handlerResourceNotFoundException(
            RuntimeException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                request.getDescription(false));

        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("original_error", ex.getMessage());
        debugInfo.put("suggestion", "Verify the resource identifier and try again");
        errorResponse.setDetails(debugInfo);

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /*
     * Exception handler for field invalid exceptions
     * 
     * @Exception : Username, PhoneNumber, Password, UserID, USer, IdentityCard
     * 
     * @param : RuntimeExcetption, WebRequest
     * 
     * @return : ResponseEntity
     */
    @ExceptionHandler({
            UsernameInvalidException.class,
            PhoneNumberInvalidException.class,
            PasswordInvalidException.class,
            UserIDInvalidException.class,
            UserNotNullException.class,
            IdentityCardInvalidException.class
    })
    public ResponseEntity<ErrorResponse> handlerFieldInvalidException(
            RuntimeException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                request.getDescription(false));

        Map<String, Object> details = new HashMap<>();
        details.put("orginal_error", ex.getMessage());
        details.put("suggest", "...");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /*
     * Exception handler for Authentication
     */

    @ExceptionHandler({
            AuthenticationException.class,
    })
    public ResponseEntity<ErrorResponse> handlerAuthenticationException(
            AuthenticationException au, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                au.getClass().getSimpleName(),
                au.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
}
