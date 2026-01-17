# API Documentation - Sensor Data Endpoints

## 📡 API cho ESP32 (POST Data)

### 1. Nhận dữ liệu từ ESP32
```
POST http://localhost:8080/api/v1/devices/data
```

**Request Body:**
```json
{
  "deviceCode": "ESP32_001",
  "value": 350.5,
  "typeSensor": "MQ2"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Dữ liệu đã được xử lý thành công",
  "data": null
}
```

---

## 🌐 API cho NextJS (GET Data)

### 2. Lấy tất cả dữ liệu cảm biến (có phân trang)
```
GET http://localhost:8080/api/v1/devices/{deviceCode}/sensor-data?page=0&size=20
```

**Example:**
```
GET http://localhost:8080/api/v1/devices/ESP32_001/sensor-data?page=0&size=20
```

**Response:**
```json
{
  "status": "success",
  "message": "Lấy dữ liệu thành công",
  "data": {
    "content": [
      {
        "id": 123,
        "deviceCode": "ESP32_001",
        "deviceName": "Cảm biến tầng 1",
        "value": 350.5,
        "typeSensor": "MQ2",
        "timestamp": "2024-01-11T10:30:00Z"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "number": 0,
    "size": 20
  }
}
```

### 3. Lấy dữ liệu theo loại cảm biến
```
GET http://localhost:8080/api/v1/devices/{deviceCode}/sensor-data/{typeSensor}?page=0&size=20
```

**Example:**
```
GET http://localhost:8080/api/v1/devices/ESP32_001/sensor-data/MQ2?page=0&size=20
GET http://localhost:8080/api/v1/devices/ESP32_001/sensor-data/DHT22?page=0&size=10
```

**Loại cảm biến:** `MQ2` hoặc `DHT22`

### 4. Lấy dữ liệu mới nhất (Real-time Dashboard)
```
GET http://localhost:8080/api/v1/devices/{deviceCode}/latest
```

**Example:**
```
GET http://localhost:8080/api/v1/devices/ESP32_001/latest
```

**Response:**
```json
{
  "status": "success",
  "message": "Lấy dữ liệu thành công",
  "data": {
    "deviceCode": "ESP32_001",
    "deviceName": "Cảm biến tầng 1",
    "mq2Value": 350.5,
    "mq2Timestamp": "2024-01-11T10:30:00Z",
    "dht22Value": 25.5,
    "dht22Timestamp": "2024-01-11T10:30:00Z",
    "deviceStatus": "ACTIVE",
    "lastActiveTime": "2024-01-11T10:30:00Z"
  }
}
```

**Device Status:**
- `ACTIVE`: Bình thường (màu xanh)
- `WARNING`: Cảnh báo (màu vàng)
- `DANGER`: Nguy hiểm (màu đỏ)
- `OFFLINE`: Mất kết nối

### 5. Lấy dữ liệu trong khoảng thời gian
```
GET http://localhost:8080/api/v1/devices/{deviceCode}/sensor-data/range?startTime={ISO8601}&endTime={ISO8601}
```

**Example:**
```
GET http://localhost:8080/api/v1/devices/ESP32_001/sensor-data/range?startTime=2024-01-01T00:00:00Z&endTime=2024-01-31T23:59:59Z
```

**Response:**
```json
{
  "status": "success",
  "message": "Lấy dữ liệu thành công",
  "data": [
    {
      "id": 123,
      "deviceCode": "ESP32_001",
      "deviceName": "Cảm biến tầng 1",
      "value": 350.5,
      "typeSensor": "MQ2",
      "timestamp": "2024-01-11T10:30:00Z"
    }
  ]
}
```

### 6. Lấy 10 bản ghi mới nhất
```
GET http://localhost:8080/api/v1/devices/{deviceCode}/recent
```

**Example:**
```
GET http://localhost:8080/api/v1/devices/ESP32_001/recent
```

---

## 🎯 Use Cases cho NextJS

### Dashboard Real-time
```typescript
// Lấy dữ liệu mới nhất cho dashboard
const response = await fetch('http://localhost:8080/api/v1/devices/ESP32_001/latest');
const { data } = await response.json();

// Hiển thị:
// - MQ2 Value: {data.mq2Value}
// - Status: {data.deviceStatus}
// - Last Update: {data.lastActiveTime}
```

### Chart/Graph (Historical Data)
```typescript
// Lấy dữ liệu 7 ngày gần đây
const startTime = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString();
const endTime = new Date().toISOString();

const response = await fetch(
  `http://localhost:8080/api/v1/devices/ESP32_001/sensor-data/range?startTime=${startTime}&endTime=${endTime}`
);
const { data } = await response.json();

// Vẽ chart với data
```

### Paginated Table
```typescript
// Lấy dữ liệu có phân trang
const response = await fetch(
  'http://localhost:8080/api/v1/devices/ESP32_001/sensor-data?page=0&size=20'
);
const { data } = await response.json();

// Hiển thị table với:
// - data.content (array dữ liệu)
// - data.totalPages (tổng số trang)
// - data.number (trang hiện tại)
```

---

## 🔥 Logic xử lý ngưỡng cảnh báo

Khi ESP32 gửi dữ liệu MQ2, hệ thống tự động:

1. **Lưu vào database** (bảng `sensor_data`)
2. **Cập nhật trạng thái thiết bị:**
   - `value > dangerThreshold` → Status = `DANGER` 🔴
   - `value > warningThreshold` → Status = `WARNING` 🟡
   - `value <= warningThreshold` → Status = `ACTIVE` 🟢
3. **Cập nhật lastActiveTime** (để phát hiện thiết bị offline)

---

## 📊 Database Schema

### Table: `sensor_data`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| device_id | BIGINT | Foreign key → devices.id |
| value | DOUBLE | Giá trị cảm biến |
| type_sensor | VARCHAR | MQ2 hoặc DHT22 |
| timestamp | TIMESTAMP | Thời điểm ghi nhận |

### Table: `devices`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| device_code | VARCHAR | Mã thiết bị (unique) |
| name_device | VARCHAR | Tên thiết bị |
| status | VARCHAR | ACTIVE/WARNING/DANGER/OFFLINE |
| warning_threshold | DOUBLE | Ngưỡng cảnh báo |
| danger_threshold | DOUBLE | Ngưỡng nguy hiểm |
| last_active_time | TIMESTAMP | Lần cuối hoạt động |

---

## 🚀 Next Steps

1. **Tạo device trong database:**
```sql
INSERT INTO devices (device_code, name_device, status, warning_threshold, danger_threshold, last_active_time)
VALUES ('ESP32_001', 'Cảm biến tầng 1', 'ACTIVE', 300.0, 500.0, NOW());
```

2. **Test API với Postman/Thunder Client**

3. **Integrate vào NextJS:**
   - Real-time monitoring với `/latest` endpoint
   - Historical chart với `/sensor-data/range` endpoint
   - Data table với `/sensor-data` endpoint (pagination)
