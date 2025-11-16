package com.example.fas.exceptions;

import com.example.fas.exceptions.auth.RefreshTokenInvalidException;
import com.example.fas.exceptions.user.error.HadUserActiveException;
import com.example.fas.exceptions.user.error.HadUserBannedException;
import com.example.fas.exceptions.user.error.HadUserDeteleException;
import com.example.fas.exceptions.user.error.HadUserRoleAdminException;
import com.example.fas.exceptions.user.exists.*;
import com.example.fas.exceptions.user.invalid.*;
import com.example.fas.exceptions.user.notFound.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
    @Getter
    @Setter
    public static class ErrorResponse {
        private int status;
        private String timestamp;
        private String error;
        private String message;
        private String path;
        private Map<String, Object> details;

        public ErrorResponse() {
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        public ErrorResponse(int status, String error, String message, String path) {
            this();
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = extractPath(path);
        }

        private String extractPath(String requestDescription) {
            if (requestDescription != null && requestDescription.startsWith("uri=")) {
                return requestDescription.substring(4);
            }
            return requestDescription;
        }
    }

    /**
     * @param ex      The exception thrown when the HTTP message is not readable.
     * @param request The current web request.
     * @return ResponseEntity<ErrorResponse> A response entity containing the error response and HTTP
     * @brief Handle HttpMessageNotReadableException and return a structured error response.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                      WebRequest request) {
        String userMessage = "Invalid JSON format.";
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
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getClass().getSimpleName(),
                userMessage, request.getDescription(false));
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("original_error", originalMessage);
        debugInfo.put("suggestion", "Check JSON syntax and field types");
        debugInfo.put("common_causes", "Missing commas, unclosed brackets, incorrect field types");
        errorResponse.setDetails(debugInfo);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @param ex      The exception thrown.
     * @param request The current web request.
     * @return ResponseEntity<ErrorResponse> A response entity containing the error response and HTTP
     * @brief Handle all other exceptions and return a structured error response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getClass().getSimpleName(), "An unexpected error occurred. Please try again later.",
                request.getDescription(false));
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("original_error", ex.getMessage());
        debugInfo.put("suggestion", "Contact support if the issue persists");
        errorResponse.setDetails(debugInfo);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param request The current web request.
     * @return ResponseEntity<ErrorResponse> A response entity containing the error response and HTTP
     * @brief Handle FieldExistsException and return a structured error response.
     */
    @ExceptionHandler({UsernameExistsException.class, PhoneNumberExistsException.class,
            IdentityCardExistsException.class, EmailExistsException.class})
    public ResponseEntity<ErrorResponse> handlerFieldExistsException(RuntimeException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getClass().getSimpleName(),
                ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({HadUserActiveException.class, HadUserBannedException.class, HadUserDeteleException.class,
            HadUserRoleAdminException.class})
    public ResponseEntity<ErrorResponse> handlerSetFieldException(RuntimeException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getClass().getSimpleName(),
                ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler({UserIDNotFoundException.class, UsernameNotFoundException.class,
            PhoneNumberNotFoundException.class, IdentityCardNotFoundException.class, EmailNotFoundException.class})
    public ResponseEntity<ErrorResponse> handlerResourceNotFoundException(RuntimeException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getClass().getSimpleName(),
                ex.getMessage(), request.getDescription(false));
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("original_error", ex.getMessage());
        debugInfo.put("suggestion", "Verify the resource identifier and try again");
        errorResponse.setDetails(debugInfo);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({UsernameInvalidException.class, PhoneNumberInvalidException.class,
            PasswordInvalidException.class, UserIDInvalidException.class, UserNotNullException.class,
            IdentityCardInvalidException.class, EmailInvalidException.class})
    public ResponseEntity<ErrorResponse> handlerFieldInvalidException(RuntimeException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getClass().getSimpleName(),
                ex.getMessage(), request.getDescription(false));
        Map<String, Object> details = new HashMap<>();
        details.put("original_error", ex.getMessage());
        details.put("suggest", "Validation failed");
        errorResponse.setDetails(details);
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * @param au      The exception thrown during authentication.
     * @param request The current web request.
     * @return ResponseEntity<ErrorResponse> A response entity containing the error response and HTTP
     * @brief Handle AuthenticationException and return a structured error response.
     */
    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ErrorResponse> handlerAuthenticationException(AuthenticationException au,
                                                                        WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), au.getClass().getSimpleName(),
                au.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * @param request ade The exception thrown when access is denied.
     * @param request The current web request.
     * @return ResponseEntity<ErrorResponse> A response entity containing the error response and HTTP
     * @brief Handle AccessDeniedException and return a structured error response.
     */

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ade, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), ade.getClass().getSimpleName(),
                "Access is denied. You do not have required permissions.", request.getDescription(false));
        Map<String, Object> debug = new HashMap<>();
        debug.put("original_error", ade.getMessage());
        debug.put("suggestion", "Check user roles/authorities for this endpoint");
        errorResponse.setDetails(debug);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * @param ex      The exception thrown when a data integrity violation occurs.
     * @param request The current web request.
     * @return ResponseEntity<ErrorResponse> A response entity containing the error response and HTTP
     * @brief Handle DataIntegrityViolationException and return a structured error response.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                             WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getClass().getSimpleName(),
                "Database constraint violation", request.getDescription(false));
        Map<String, Object> debug = new HashMap<>();
        debug.put("original_error", ex.getMessage());
        errorResponse.setDetails(debug);
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * @param ex      The exception thrown when authentication is insufficient.
     * @param request The current web request.
     * @return ResponseEntity<ErrorResponse> A response entity containing the error response and HTTP
     * @brief Handle InsufficientAuthenticationException and return a structured error response.
     */
    @ExceptionHandler({
            InsufficientAuthenticationException.class
    })
    public ResponseEntity<ErrorResponse> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getClass().getSimpleName(),
                "Authentication is required to access this resource.",
                request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * @param ex      The exception thrown during OAuth2 authentication.
     * @param request The current web request.
     * @return ResponseEntity<ErrorResponse> A response entity containing the error response and HTTP
     * @brief Handle OAuth2AuthenticationException and related exceptions, returning a structured error response.
     */
    @ExceptionHandler({
            OAuth2AuthenticationException.class,
            AccountSocialExistsException.class,
            ProviderNotSupportException.class
    })
    public ResponseEntity<ErrorResponse> handleAccountSocialExistsException(RuntimeException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * @param ex      The exception thrown when the refresh token is invalid.
     * @param request The current web request.
     * @return ResponseEntity<ErrorResponse> A response entity containing the error response and HTTP
     * @brief Handle RefreshTokenInvalidException and return a structured error response.
     */
    @ExceptionHandler({
            RefreshTokenInvalidException.class
    })
    public ResponseEntity<ErrorResponse> handlerRefreshTokenInvalidException(RefreshTokenInvalidException ex,
                                                                             WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * @param ex      The exception thrown when method arguments are not valid.
     * @param request The current web request.
     * @return ResponseEntity<ErrorResponse> A response entity containing the error response and HTTP
     * @brief Handle MethodArgumentNotValidException and return a structured error response.
     */
    @ExceptionHandler({
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ErrorResponse> handlerMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                             WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getClass().getSimpleName(),
                "Username or password invalid",
                request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}