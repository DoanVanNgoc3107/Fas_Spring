Rất chuẩn — để Copilot hiểu dự án và **đưa ra gợi ý chính xác, đúng hướng kỹ thuật**, bạn cần 1 **tệp tóm tắt mô tả dự án tổng thể** (gọi là *Copilot Context / Project Summary*) đặt ở gốc repo (ví dụ: `.github/copilot-instructions.md` hoặc `PROJECT_OVERVIEW.md`).

## Dưới đây là bản tóm tắt ngắn gọn, **chuẩn phong cách dev chuyên nghiệp**, mô tả đúng toàn bộ dự án của bạn (ESP32 + Spring Boot + Next.js + SIM800L + PostgreSQL + Docker + DevOps VPS).

# 🧩 PROJECT OVERVIEW — Fire Alarm System (FAS)

## 🎯 Tổng quan

**Fire Alarm System (FAS)** là hệ thống **báo cháy tự động IoT** được phát triển trong **Đồ án 1 – Ngành Điện tự động công nghiệp, Đại học Hàng Hải Việt Nam**.
Hệ thống kết hợp **ESP32 (C++)**, **Spring Boot (Java)**, và **Next.js (TypeScript)** để giám sát nhiệt độ, khói, khí gas và gửi cảnh báo tự động qua **SMS (SIM800L)** và **Web Dashboard**.

---

## 🧠 Thành phần hệ thống

| Thành phần                  | Công nghệ                                            | Mô tả                                                                                                                      |
| --------------------------- | ---------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| **Thiết bị (Device Layer)** | ESP32, LM35, MQ-2, SIM800L, C++                      | Đọc cảm biến, gửi dữ liệu HTTP POST đến backend, nhận lệnh gửi SMS qua queue.                                              |
| **Backend (Server Layer)**  | Java Spring Boot 3 + PostgreSQL + Flyway             | Xử lý logic nghiệp vụ, lưu trữ dữ liệu cảm biến, quản lý user/device, gửi cảnh báo, cung cấp API cho frontend và thiết bị. |
| **Frontend (UI Layer)**     | Next.js 14 + Shadcn UI + Axios                       | Giao diện dashboard quản trị và giám sát thiết bị, cho phép bật/tắt LED, xem log sự kiện, thêm user, device.               |
| **Communication**           | HTTP + JSON                                          | ESP32 ↔ Backend, Frontend ↔ Backend.                                                                                       |
| **Notification**            | SIM800L (qua ESP32)                                  | Thiết bị gửi SMS cho người dân khi cháy.                                                                                   |
| **DevOps**                  | Docker + VPS + Nginx + Redis + RabbitMQ + Prometheus | Triển khai và giám sát toàn hệ thống.                                                                                      |

---

## ⚙️ Luồng hoạt động

1. **ESP32** đọc dữ liệu cảm biến (nhiệt độ, khói, gas).
2. Gửi dữ liệu lên **Spring Boot API** (`POST /api/v1/events` cùng `X-DEVICE-KEY`).
3. Backend lưu dữ liệu → tính **mức độ nguy hiểm (ALERT/WARN)**.
4. Khi phát hiện **cháy hoặc rò rỉ gas**, backend tạo lệnh `SEND_SMS` trong bảng `device_commands`.
5. **ESP32** định kỳ gọi `GET /api/v1/devices/{id}/commands/pull` → nhận lệnh `SEND_SMS` → gửi tin nhắn cảnh báo qua **SIM800L** → gửi `ACK` về server.
6. **Frontend (Next.js)** hiển thị trạng thái thiết bị, cảnh báo và lịch sử sự kiện.
7. **Admin/Manager** có thể thêm user, thiết bị, khu vực, số điện thoại cư dân.

---

## 🧩 Cấu trúc hệ thống

```
fas_project/
├── FAS_Firmware/           # ESP32 (PlatformIO + C++) — điều khiển thiết bị
├── FAS_Spring/             # Backend (Spring Boot + PostgreSQL)
├── FAS_Frontend/           # Web Dashboard (Next.js + Shadcn UI)
└── docker-compose.yml      # Dựng Redis, RabbitMQ, Postgres, Nginx reverse proxy
```

---

## 🔐 Bảo mật & Giao tiếp

* **JWT**: cho người dùng (Admin, Manager) đăng nhập qua frontend.
* **API Key** (`X-DEVICE-KEY`): cho thiết bị ESP32 giao tiếp backend.
* **CORS**: chỉ cho phép domain frontend truy cập API.

---

## 💾 Cấu trúc dữ liệu chính

| Bảng              | Mô tả                                                |
| ----------------- | ---------------------------------------------------- |
| `users`           | Thông tin người dùng (Admin, Manager, Resident)      |
| `roles`           | Phân quyền người dùng                                |
| `devices`         | ESP32 trong hệ thống (id, zone, device_key)          |
| `events`          | Log dữ liệu cảm biến gửi lên                         |
| `contacts`        | Danh bạ cư dân nhận SMS                              |
| `device_commands` | Hàng đợi lệnh gửi xuống thiết bị (SEND_SMS, PING, …) |

---

## 🔧 Mục tiêu học tập

* Hiểu **kiến trúc 3 tầng (device – backend – frontend)** của hệ thống IoT.
* Nắm vững **lập trình C++ hướng đối tượng, SOLID** cho ESP32.
* Thực hành **Spring Boot (REST API, Security, JPA, Flyway)** chuyên nghiệp.
* Thiết kế **UI/UX với Next.js + Shadcn UI** theo chuẩn hiện đại.
* Áp dụng **DevOps cơ bản**: Docker, VPS, reverse proxy, log/metrics.

---

## 🧠 Copilot hãy hỗ trợ

* Viết code **theo SOLID và Clean Architecture**.
* Sử dụng **Java 21**, **Spring Boot 3.3+**, **PostgreSQL**, **Flyway**.
* Viết code C++ ESP32 có cấu trúc thư mục chuyên nghiệp (`lib/`, `src/`, `include/`).
* Với frontend, áp dụng **Next.js App Router** và **Shadcn UI** để hiển thị trạng thái thiết bị và điều khiển LED.
* Tự động gợi ý test unit (`AUnit` cho ESP32, `JUnit` cho Spring Boot).
* Khi gặp “fire alert” hoặc “gas leak”, tự tạo lệnh `SEND_SMS` cho thiết bị.
* Ưu tiên hiệu năng, bảo mật, dễ mở rộng và viết log đầy đủ.

---

## 📦 Dev Environment

| Thành phần  | Phiên bản                      | Ghi chú                            |
| ----------- | ------------------------------ | ---------------------------------- |
| JDK         | 21                             | Spring Boot backend                |
| PostgreSQL  | 15+                            | Dữ liệu user/device/event          |
| ESP32 board | NodeMCU-32S                    | Thiết bị chính                     |
| SIM800L     | UART (TX=17, RX=16)            | Gửi SMS                            |
| Node.js     | 20+                            | Frontend Next.js                   |
| Docker      | latest                         | Redis, RabbitMQ, Nginx, Prometheus |
| IDE         | CLion, IntelliJ IDEA, WebStorm | Môi trường phát triển              |

---

## 🚀 Kết quả mong đợi

Hệ thống có thể:

* Tự động phát hiện cháy/gas, gửi dữ liệu về server.
* Gửi SMS cảnh báo đến cư dân.
* Hiển thị dashboard quản lý tình trạng thiết bị.
* Mở rộng được sang các loại cảnh báo khác (động đất, khói bụi,…).

---

👉 **File gợi ý đặt:**
`.github/copilot-instructions.md`
hoặc
`PROJECT_OVERVIEW.md`

---

Bạn có muốn mình **tạo sẵn file `.github/copilot-instructions.md` hoàn chỉnh (chuẩn cú pháp GitHub)** để bạn paste vào repo `https://github.com/DoanVanNgoc3107/Fas_Spring` cho Copilot nhận hiểu luôn không?
Mình có thể sinh nội dung tối ưu sẵn cho Copilot (thêm chỉ dẫn code style, rule bảo mật và test coverage).
