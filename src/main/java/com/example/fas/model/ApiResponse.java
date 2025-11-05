package com.example.fas.model;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * ApiResponse - Cấu trúc response chuẩn cho tất cả API
 */
@Data
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
    private Object error;
    private LocalDateTime timestamp;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(HttpStatus httpStatus, String message, T data, Object error) {
        this.status = httpStatus.value();
        this.message = message;
        this.data = data;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    // Static methods để tạo response nhanh
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(HttpStatus.OK, message, data, null);
    }

    public static <T> ApiResponse<T> error(String message, Object error) {
        return new ApiResponse<>(HttpStatus.BAD_REQUEST, message, null, error);
    }

    // Builder methods để tương thích với code hiện tại
    public static <T> ApiResponseBuilder<T> builder() {
        return new ApiResponseBuilder<>();
    }

    public static class ApiResponseBuilder<T> {
        private int status = 200;
        private String message;
        private T data;
        private Object error;
        private LocalDateTime timestamp = LocalDateTime.now();

        public ApiResponseBuilder<T> status(int status) {
            this.status = status;
            return this;
        }

        public ApiResponseBuilder<T> message(String message) {
            this.message = message;
            return this;
        }

        public ApiResponseBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ApiResponseBuilder<T> error(Object error) {
            this.error = error;
            return this;
        }

        public ApiResponseBuilder<T> timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /*
         * Phương thức build để tạo đối tượng ApiResponse từ builder
         * */
        public ApiResponse<T> build() {
            ApiResponse<T> response = new ApiResponse<>();
            response.setStatus(this.status);
            response.setMessage(this.message);
            response.setData(this.data);
            response.setError(this.error);
            response.setTimestamp(this.timestamp);
            return response;
        }
    }
}