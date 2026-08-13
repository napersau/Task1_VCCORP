# Gold Price API

Backend quản lý và tra cứu giá vàng bằng Java 17/Spring Boot. Dự án hỗ trợ CRUD, tìm kiếm và phân trang, đồng bộ dữ liệu định kỳ, Redis Cache, Swagger/OpenAPI, logging tập trung và đóng gói Docker.

## 1. Tính năng

- REST API CRUD theo chuẩn HTTP, validation và lỗi JSON thống nhất.
- Spring Data JPA với H2 file, PostgreSQL hoặc MySQL.
- Tìm kiếm theo loại vàng, phân trang và sắp xếp.
- Scheduler/WebClient đồng bộ nguồn ngoài và loại dữ liệu trùng.
- MapStruct tách Entity khỏi DTO trả cho client.
- Redis Cache cho API đọc, tự xóa cache khi dữ liệu thay đổi.
- Swagger UI, Postman Collection, request ID và Logback rolling file.
- Unit test Mockito, Repository test và MockMvc integration test.
- JAR thực thi độc lập, Dockerfile và Docker Compose.

## 2. Yêu cầu môi trường

Chọn một trong hai cách chạy:

| Cách chạy | Yêu cầu |
|---|---|
| Local tối giản | JDK 17, Maven 3.9+ |
| Docker đầy đủ | Docker Desktop/Engine và Docker Compose v2 |

Kiểm tra công cụ local:

```powershell
java -version
mvn -version
```

## 3. Chạy nhanh sau khi clone

### Local không cần cài Database/Redis

```powershell
git clone https://github.com/napersau/Task1_VCCORP.git
cd Task1_VCCORP/task1
mvn clean test
mvn spring-boot:run
```

Chế độ mặc định sử dụng H2 file tại `task1/data/gold-price.mv.db`, cache in-memory và tắt Scheduler. Kiểm tra:

- Health: `http://localhost:8080/api/gold-prices/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Docker với PostgreSQL và Redis

Tại thư mục gốc:

```powershell
Copy-Item .env.example .env
docker compose up --build -d
docker compose ps
Invoke-RestMethod http://localhost:8080/api/gold-prices/health
```

Compose khởi chạy `app`, PostgreSQL và Redis. Dữ liệu/log được giữ trong named volumes. Dừng mà vẫn giữ dữ liệu:

```powershell
docker compose down
```

Chỉ dùng `docker compose down -v` khi thực sự muốn xóa toàn bộ dữ liệu và log trong volumes.

## 4. Cấu hình

Không commit mật khẩu hoặc API key thật. Sao chép [.env.example](.env.example) thành `.env` hoặc đặt biến môi trường trực tiếp.

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `SERVER_PORT` | `8080` | Cổng HTTP |
| `SPRING_PROFILES_ACTIVE` | trống | `postgres` hoặc `mysql` |
| `DB_URL` | H2 file | JDBC URL |
| `DB_USERNAME`, `DB_PASSWORD` | `sa`, trống | Tài khoản DB |
| `CACHE_TYPE` | `simple` | `simple`, `redis` hoặc `none` |
| `REDIS_HOST`, `REDIS_PORT` | `localhost`, `6379` | Redis endpoint |
| `CACHE_TTL` | `10m` | Thời gian sống cache |
| `GOLD_PRICE_SCHEDULER_ENABLED` | `false` | Bật job đồng bộ |
| `GOLD_PRICE_SOURCE_URL` | URL vô hiệu | API nguồn giá vàng |
| `GOLD_PRICE_SOURCE_API_KEY` | trống | API key tùy chọn |
| `GOLD_PRICE_SCHEDULER_CRON` | mỗi 5 phút | Cron 6 trường của Spring |
| `GOLD_PRICE_SCHEDULER_ZONE` | `Asia/Ho_Chi_Minh` | Múi giờ Scheduler |
| `LOG_PATH` | `logs` | Thư mục log |
| `APP_LOG_LEVEL` | `INFO` | Mức log ứng dụng |

Ví dụ chạy local với PostgreSQL/Redis:

```powershell
$env:SPRING_PROFILES_ACTIVE="postgres"
$env:DB_URL="jdbc:postgresql://localhost:5432/gold_price_db"
$env:DB_USERNAME="gold_user"
$env:DB_PASSWORD="your-password"
$env:CACHE_TYPE="redis"
$env:REDIS_HOST="localhost"
mvn spring-boot:run
```

## 5. Đồng bộ giá vàng

Scheduler chỉ nên bật sau khi cấu hình nguồn thật:

```powershell
$env:GOLD_PRICE_SCHEDULER_ENABLED="true"
$env:GOLD_PRICE_SOURCE_URL="https://provider.example/api/gold-prices"
$env:GOLD_PRICE_SOURCE_API_KEY="optional-key"
```

Nguồn trả về JSON array:

```json
[
  { "goldType": "SJC", "buyPrice": 80000000, "sellPrice": 82000000 }
]
```

Các alias `gold_type`/`type`/`name`, `buy_price`/`buy`, `sell_price`/`sell` cũng được hỗ trợ. Dữ liệu thiếu, giá âm, giá bán thấp hơn giá mua hoặc trùng loại vàng và mức giá sẽ bị bỏ qua. Một lần chạy lỗi không làm dừng các lần cron kế tiếp.

## 6. API

Base URL: `http://localhost:8080/api/gold-prices`

| Method | Endpoint | Kết quả |
|---|---|---|
| GET | `/health` | Trạng thái dịch vụ |
| GET | `?goldType=SJC&page=0&size=10&sortBy=updatedAt&direction=desc` | Tìm kiếm/phân trang |
| GET | `/{id}` | Chi tiết theo ID |
| POST | `/` | Tạo mới, trả `201` |
| PUT | `/{id}` | Cập nhật |
| DELETE | `/{id}` | Xóa, trả `204` |

POST/PUT body:

```json
{ "goldType": "SJC", "buyPrice": 80000000, "sellPrice": 82000000 }
```

`size` nằm trong 1–100. `sortBy` nhận `id`, `goldType`, `buyPrice`, `sellPrice`, `updatedAt`; `direction` nhận `asc` hoặc `desc`.

## 7. Lỗi và logging

Lỗi trả về format an toàn và có `requestId`:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Dữ liệu đầu vào không hợp lệ",
  "path": "/api/gold-prices",
  "requestId": "97272c19-3fc1-480b-a47c-c540ce794133",
  "timestamp": "2026-08-12T09:00:00Z",
  "fieldErrors": { "goldType": "Loại vàng không được để trống" }
}
```

Client có thể gửi `X-Request-ID`; nếu thiếu, server tự sinh và trả lại trong header. Log được ghi ra console và `task1/logs/gold-price-api.log`, xoay mỗi ngày/10 MB và giữ 14 ngày. Scheduler ghi một log INFO lúc bắt đầu và một log kết quả lúc kết thúc; chi tiết xử lý nội bộ ở DEBUG để tránh log INFO trùng lặp.

## 8. Test, đóng gói và chạy JAR

```powershell
cd task1
mvn clean test
mvn clean package
java -jar target/gold-price-api-0.0.1-SNAPSHOT.jar
```

Khi chạy JAR, thư mục làm việc quyết định vị trí `data/` và `logs/`. Có thể đổi bằng `DB_URL` và `LOG_PATH`. Smoke test từ terminal khác:

```powershell
Invoke-RestMethod http://localhost:8080/api/gold-prices/health
Invoke-WebRequest http://localhost:8080/v3/api-docs -UseBasicParsing
```

Hoặc chạy smoke test tự động; script dùng H2 in-memory, cổng `18086` và luôn dừng tiến trình JAR sau khi kiểm tra:

```powershell
cd ..
powershell -ExecutionPolicy Bypass -File scripts/smoke-test-jar.ps1
```

## 9. Tài liệu bàn giao

- [Kiến trúc hệ thống](docs/ARCHITECTURE.md)
- [Tổng kết kỳ thực tập](docs/FINAL_INTERNSHIP_SUMMARY.md)
- [Postman Collection](postman/Gold-Price-API-Week5.postman_collection.json)
- Swagger UI: `/swagger-ui.html`

## 10. Xử lý sự cố

| Hiện tượng | Cách kiểm tra |
|---|---|
| Port 8080 đã dùng | Đặt `SERVER_PORT` sang cổng khác |
| Không kết nối DB | Kiểm tra profile, JDBC URL, user/password và container health |
| Redis lỗi | Kiểm tra `CACHE_TYPE`, host/port; local có thể dùng `CACHE_TYPE=simple` |
| Scheduler không chạy | Kiểm tra cờ enabled, cron, URL nguồn và log ứng dụng |
| Swagger không mở | Thử `/v3/api-docs`, kiểm tra port và application log |
| Tiếng Việt trong PowerShell lỗi | Dùng terminal UTF-8; file source và response đều ở UTF-8 |

## 11. Cấu trúc chính

```text
task1/src/main/java/com/example/goldprice
├── config          # cache, scheduling, OpenAPI, request ID, CORS
├── controller      # REST endpoints
├── dto             # request/response contract
├── exception       # exception và handler tập trung
├── integration     # WebClient đọc nguồn ngoài
├── mapper          # MapStruct
├── model           # JPA entity
├── repository      # Spring Data JPA
├── scheduler       # cron job
└── service         # nghiệp vụ và đồng bộ dữ liệu
```
