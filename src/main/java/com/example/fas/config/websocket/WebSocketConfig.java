package com.example.fas.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import lombok.RequiredArgsConstructor;

/**
 * WebSocket Configuration
 * Đăng ký WebSocket endpoint cho ESP32 devices kết nối
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ESP32WebSocketHandler esp32WebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(esp32WebSocketHandler, "/ws/esp32")
                .setAllowedOrigins("*"); // Cho phép ESP32 từ bất kỳ địa chỉ nào kết nối
    }
}
