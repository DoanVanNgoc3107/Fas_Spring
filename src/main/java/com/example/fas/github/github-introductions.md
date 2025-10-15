# 🤖 Copilot Context Guide – Fire Alarm System for Homestay (IoT Project)

## 🏫 Project Information
**Tên dự án:** Hệ thống Báo Cháy Cho Homestay  
**Ngành học:** Tự động hóa Công nghiệp (Đại học Hàng Hải Việt Nam)  
**Nhóm sinh viên:**
| Họ và Tên | MSSV | Lớp | Vai trò |
|------------|-------|------|-----------|
| **Đoàn Văn Ngọc** | 104317 | DTD64CL | Leader |
| **Trần Ích Hưng** | 104323 | DTD64CL | Member |
| **Lê Việt Hoàng** | 104329 | DTD64CL | Member |

---

## 🧩 Project Overview
Dự án này là một **hệ thống IoT báo cháy cho homestay**, được xây dựng với **3 thành phần chính**:

- **Firmware:** Arduino Uno R3 + ESP32 + SIM800L
- **Backend:** Java Spring Boot + PostgreSQL
- **Frontend:** Next.js (TypeScript + Shadcn UI)

Mục tiêu là **phát hiện sớm cháy nổ** thông qua cảm biến **nhiệt độ, khói và gas**, sau đó **tự động gửi cảnh báo SMS** cho **chủ nhà và khách thuê** thông qua module SIM800L, đồng thời **hiển thị cảnh báo real-time trên web**.

---

## ⚙️ Main Features

### 🧠 Backend (Spring Boot)
- Quản lý người dùng (Admin / User), đăng ký, đăng nhập, phân quyền.
- Xác thực JWT + OAuth2 (Google, Facebook).
- Quản lý phòng (CRUD), thông tin khách thuê.
- Tích hợp thanh toán online qua **Stripe API**.
- Nhận dữ liệu cảm biến từ ESP32 qua HTTP/MQTT.
- Khi vượt ngưỡng → gửi cảnh báo **SMS qua SIM800L** và **notification real-time qua WebSocket**.
- Lưu log cảm biến vào PostgreSQL và hiển thị qua dashboard.

### 💻 Frontend (Next.js + Shadcn UI)
- Giao diện **đăng nhập/đăng ký**, **quản lý phòng**, **đặt phòng**.
- Hiển thị **trạng thái cảm biến nhiệt độ, khói, gas** theo thời gian thực.
- Hiển thị **popup cảnh báo cháy**, âm thanh cảnh báo và màu sắc trực quan.
- **Trang quản trị (Admin Panel)** cho phép quản lý user, phòng và lịch sử cảnh báo.

### 🔩 Firmware (ESP32 + Arduino)
- Sử dụng các cảm biến:
    - DHT11 → đo nhiệt độ & độ ẩm
    - MQ-2 → phát hiện khói
    - MQ-5 → phát hiện gas
- Gửi dữ liệu cảm biến lên server qua **HTTP hoặc MQTT**.
- Khi **mất WiFi**, gửi **SMS cảnh báo** qua module **SIM800L**.
- Có **nguồn dự phòng (pin Li-ion)** đảm bảo hoạt động liên tục khi mất điện.

---

## 🧱 Folder Structure (Clean Architecture)


---

## 🔌 Port Configuration
| Thành phần | Port | Mô tả |
|-------------|-------|-------|
| Backend | `8080` | REST API |
| Frontend | `3000` | Next.js UI |
| Database | `5432` | PostgreSQL |
| ESP32 Firmware | `80` | HTTP/MQTT |

---

## 💬 Copilot Coding Context
### 🧠 Copilot cần hiểu rõ:
- Dự án bao gồm 3 phần riêng biệt (Firmware, Backend, Frontend) nhưng **liên kết qua HTTP/MQTT & WebSocket**.
- Mọi gợi ý code nên tuân thủ:
    - Java 21+
    - Spring Boot 3.x (RESTful API, Security, Lombok, JPA, PostgreSQL)
    - React 18 / Next.js 14 (TypeScript, Tailwind, Shadcn UI)
    - ESP32 với Arduino Framework (C++/PlatformIO)
---

## 💡 Tips cho Copilot
> Hướng dẫn Copilot hiểu chính xác ngữ cảnh bằng comment trong code.

**Ví dụ (ESP32):**
```cpp
// [CONTEXT]: Fire Alarm IoT project - send sensor data to Spring Boot server via HTTP
// Read DHT11, MQ-2, MQ-5 sensors and send alerts if threshold exceeded.
