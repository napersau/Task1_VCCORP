# Gold Price API - Spring Boot Backend

Đây là backend mô phỏng nguồn tra cứu giá vàng cho website hiển thị. Dữ liệu hiện tại được seed sẵn vào H2 in-memory database và có thể thay bằng nguồn dữ liệu thật khi tích hợp sau này.

## Tổng Quan

Ứng dụng cung cấp 2 endpoint chính:

1. Lấy toàn bộ danh sách giá vàng hiện tại.
2. Lấy lịch sử giá theo từng loại vàng.

API trả về JSON để frontend có thể hiển thị trực tiếp.

## Công Nghệ Sử Dụng

- Java 21
- Spring Boot 3.3.x
- Spring Web
- Spring Data JPA
- Spring Validation
- H2 Database
- Maven
- JUnit 5
- MockMvc

## Cấu Trúc Project

```text
task1/
  src/main/java/com/example/goldprice
    GoldPriceApiApplication.java
    config/WebConfig.java
    controller/GoldPriceController.java
    dto/ErrorResponse.java
    dto/GoldPriceResponse.java
    dto/HealthResponse.java
    exception/GlobalExceptionHandler.java
    exception/GoldPriceNotFoundException.java
    mapper/GoldPriceMapper.java
    model/GoldPrice.java
    repository/GoldPriceRepository.java
    service/GoldPriceService.java
  src/main/resources
    application.properties
    data.sql
  src/test/java/com/example/goldprice
    controller/GoldPriceControllerTest.java
    service/GoldPriceServiceTest.java
```

## Kiến Trúc Xử Lý

Dữ liệu đi theo luồng:

Nguồn giá vàng giả lập -> Service xử lý -> Repository đọc dữ liệu H2 -> Controller trả JSON cho client.

## Model Dữ Liệu

Model hiện tại gồm 4 trường chính:

- `goldType`: loại vàng, ví dụ `SJC`, `9999`, `24K`
- `buyPrice`: giá mua vào
- `sellPrice`: giá bán ra
- `updatedAt`: thời gian cập nhật

Response trả về từ API được chuẩn hóa theo đúng bộ trường trên.

## Chạy Dự Án

Cần có Java 21 và Maven.

```powershell
cd task1
mvn spring-boot:run
```

Ứng dụng mặc định chạy tại:

```text
http://localhost:8080
```

## API Endpoints

### Health Check

```http
GET /api/gold-prices/health
```

Response mẫu:

```json
{
  "status": "ok",
  "service": "gold-price-api",
  "checkedAt": "2026-07-05T08:00:00Z"
}
```

### Lấy Toàn Bộ Giá Vàng Hiện Tại

```http
GET /api/gold-prices
```

Response mẫu:

```json
[
  {
    "goldType": "14K",
    "buyPrice": 45200000,
    "sellPrice": 47200000,
    "updatedAt": "2026-07-02T09:20:00"
  },
  {
    "goldType": "18K",
    "buyPrice": 58500000,
    "sellPrice": 60500000,
    "updatedAt": "2026-07-02T09:15:00"
  }
]
```

### Lấy Giá Vàng Theo Loại

```http
GET /api/gold-prices/{goldType}
```

Ví dụ:

```http
GET /api/gold-prices/SJC
```

Response mẫu:

```json
[
  {
    "goldType": "SJC",
    "buyPrice": 80000000,
    "sellPrice": 82000000,
    "updatedAt": "2026-07-02T09:00:00"
  }
]
```

Nếu không tìm thấy dữ liệu, API trả về HTTP `404` với body lỗi theo format `ErrorResponse`.

## Cấu Hình

- CORS được cấu hình trong `src/main/java/com/example/goldprice/config/WebConfig.java` để frontend có thể gọi API từ domain khác.
- H2 in-memory database được cấu hình trong `src/main/resources/application.properties`.
- Dữ liệu mẫu được nạp từ `src/main/resources/data.sql`.

## Dữ Liệu Demo

Dữ liệu demo hiện tại gồm các loại vàng: `SJC`, `9999`, `24K`, `18K`, `14K`.

## Test

```powershell
cd task1
mvn test
```

Bộ test bao gồm kiểm tra:

- Lấy danh sách giá vàng
- Lấy giá vàng theo loại
- Trường hợp không tìm thấy dữ liệu

## Mở Rộng Sau Này

- Kết nối nguồn giá vàng thật thay cho seed data
- Thêm cache nếu lượng request lớn
- Thêm xác thực nếu API không public
- Thêm logging và monitoring khi đưa lên production
