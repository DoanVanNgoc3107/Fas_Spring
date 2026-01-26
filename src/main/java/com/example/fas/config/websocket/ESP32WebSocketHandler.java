package com.example.fas.config.websocket;

import com.example.fas.model.Device;
import com.example.fas.repositories.services.DeviceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Handler cho ESP32 devices
 * Quản lý kết nối WebSocket và gửi lệnh alert tới ESP32
 */
@Slf4j
@Component
public class ESP32WebSocketHandler extends TextWebSocketHandler {

    private final DeviceRepository deviceRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ESP32WebSocketHandler(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    // Lưu trữ WebSocket sessions theo deviceCode
    // Key: deviceCode, Value: WebSocketSession
    private final Map<String, WebSocketSession> deviceSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New WebSocket connection established: sessionId={}", session.getId());
        log.info("Remote address: {}", session.getRemoteAddress());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received message from session {}: {}", session.getId(), payload);

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String messageType = jsonNode.get("type").asText();

            switch (messageType) {
                case "register":
                    handleRegistration(session, jsonNode);
                    break;

                case "heartbeat":
                    handleHeartbeat(session, jsonNode);
                    break;

                case "ack":
                    handleAcknowledgment(session, jsonNode);
                    break;

                case "pong":
                    log.debug("Pong received from device in session {}", session.getId());
                    break;

                default:
                    log.warn("Unknown message type: {}", messageType);
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
        }
    }

    /**
     * Xử lý registration message từ ESP32
     */
    private void handleRegistration(WebSocketSession session, JsonNode jsonNode) throws IOException {
        String deviceCode = jsonNode.get("deviceCode").asText();
        String version = jsonNode.has("version") ? jsonNode.get("version").asText() : "unknown";

        log.info("Device registration: deviceCode={}, version={}", deviceCode, version);

        // Kiểm tra device có tồn tại trong database không
        Optional<Device> deviceOpt = deviceRepository.findByDeviceCode(deviceCode);
        if (deviceOpt.isEmpty()) {
            log.warn("Device not found: {}", deviceCode);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", "Device not found: " + deviceCode
            ))));
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Device not found"));
            return;
        }

        // Lưu session
        deviceSessions.put(deviceCode, session);
        log.info("Device {} registered successfully. Total active devices: {}", 
                 deviceCode, deviceSessions.size());

        // Gửi acknowledgment
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                "type", "registered",
                "deviceCode", deviceCode,
                "message", "Registration successful"
        ))));
    }

    /**
     * Xử lý heartbeat từ ESP32
     */
    private void handleHeartbeat(WebSocketSession session, JsonNode jsonNode) {
        String deviceCode = jsonNode.get("deviceCode").asText();
        log.debug("Heartbeat from device: {}", deviceCode);
    }

    /**
     * Xử lý acknowledgment từ ESP32
     */
    private void handleAcknowledgment(WebSocketSession session, JsonNode jsonNode) {
        String action = jsonNode.get("action").asText();
        String status = jsonNode.get("status").asText();
        log.info("Device acknowledged action: {}, status: {}", action, status);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: sessionId={}, status={}", 
                 session.getId(), status);

        // Xóa session khỏi map
        deviceSessions.entrySet().removeIf(entry -> 
            entry.getValue().getId().equals(session.getId())
        );

        log.info("Active devices remaining: {}", deviceSessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error in session {}: {}", 
                  session.getId(), exception.getMessage(), exception);
    }

    /**
     * API Method: Gửi lệnh trigger alert tới ESP32
     * @param deviceCode Mã thiết bị
     * @return true nếu gửi thành công, false nếu thiết bị không online
     */
    public boolean sendTriggerAlert(String deviceCode) {
        WebSocketSession session = deviceSessions.get(deviceCode);
        
        if (session == null || !session.isOpen()) {
            log.warn("Device {} is not connected via WebSocket", deviceCode);
            return false;
        }

        try {
            String message = objectMapper.writeValueAsString(Map.of(
                    "action", "trigger_alert",
                    "timestamp", System.currentTimeMillis()
            ));

            session.sendMessage(new TextMessage(message));
            log.info("🚨 Sent trigger_alert to device: {}", deviceCode);
            return true;

        } catch (IOException e) {
            log.error("❌ Failed to send trigger_alert to device {}: {}", 
                      deviceCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * API Method: Gửi lệnh reset alert tới ESP32
     * @param deviceCode Mã thiết bị
     * @return true nếu gửi thành công, false nếu thiết bị không online
     */
    public boolean sendResetAlert(String deviceCode) {
        WebSocketSession session = deviceSessions.get(deviceCode);
        
        if (session == null || !session.isOpen()) {
            log.warn("Device {} is not connected via WebSocket", deviceCode);
            return false;
        }

        try {
            String message = objectMapper.writeValueAsString(Map.of(
                    "action", "reset_alert",
                    "timestamp", System.currentTimeMillis()
            ));

            session.sendMessage(new TextMessage(message));
            log.info("🔄 Sent reset_alert to device: {}", deviceCode);
            return true;

        } catch (IOException e) {
            log.error("❌ Failed to send reset_alert to device {}: {}", 
                      deviceCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Kiểm tra xem device có đang online (connected via WebSocket) không
     * @param deviceCode Mã thiết bị
     * @return true nếu device đang online
     */
    public boolean isDeviceOnline(String deviceCode) {
        WebSocketSession session = deviceSessions.get(deviceCode);
        return session != null && session.isOpen();
    }

    /**
     * Lấy số lượng devices đang online
     * @return Số devices đang kết nối
     */
    public int getOnlineDeviceCount() {
        return deviceSessions.size();
    }

    /**
     * Gửi ping tới device để kiểm tra kết nối
     * @param deviceCode Mã thiết bị
     */
    public void pingDevice(String deviceCode) {
        WebSocketSession session = deviceSessions.get(deviceCode);
        
        if (session != null && session.isOpen()) {
            try {
                String message = objectMapper.writeValueAsString(Map.of(
                        "action", "ping",
                        "timestamp", System.currentTimeMillis()
                ));
                session.sendMessage(new TextMessage(message));
                log.debug("🏓 Ping sent to device: {}", deviceCode);
            } catch (IOException e) {
                log.error("Failed to ping device {}: {}", deviceCode, e.getMessage());
            }
        }
    }
}
