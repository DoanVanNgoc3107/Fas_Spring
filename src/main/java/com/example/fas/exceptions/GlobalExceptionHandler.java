package com.example.fas.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
     private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

     // handle all uncaught exceptions
     @Getter
    @Setter
    public static class ErrorResponse {
        private String timestamp; // Thời gian xảy ra lỗi
        private int status; // HTTP status code (400, 404, 500...)
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

     @ExceptionHandler(HttpMessageNotReadableException.class)
     public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
               HttpMessageNotReadableException ex, WebRequest request) {

          logger.warn("📋 JSON PARSING ERROR: {}", ex.getMessage());

          // Tạo message dễ hiểu cho client
          String userMessage = "Invalid JSON format.";

          // Phân tích loại lỗi để đưa ra gợi ý cụ thể
          String originalMessage = ex.getMessage();
          if (originalMessage != null) {
               if (originalMessage.contains("Required request body is missing")) {
                    userMessage = " Request body is missing.";
               } else if (originalMessage.contains("JSON parse error")) {
                    userMessage = " Invalid JSON format.";
               } else if (originalMessage.contains("Cannot deserialize")) {
                    userMessage = " Cannot deserialize JSON. Check field types.";
               }
          }

          ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "JSON_PARSING_ERROR",
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

}
