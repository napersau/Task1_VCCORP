# Gold Price API

REST API quản lý giá vàng được xây dựng bằng Java 17 và Spring Boot. Dữ liệu được lưu bền vững thay cho mock data; API hỗ trợ CRUD, tìm kiếm, phân trang, Redis cache và đồng bộ tự động từ nguồn HTTP.

## Công nghệ

- Java 17, Spring Boot 3.3
- Spring Web, Validation, Data JPA
- WebClient và Spring Scheduler
- MapStruct cho Entity–DTO mapping
- Spring Cache và Redis
- Springdoc OpenAPI/Swagger UI
- H2 file (mặc định để chạy nhanh), MySQL hoặc PostgreSQL qua profile
- HikariCP connection pool
- JUnit 5, MockMvc, `@DataJpaTest`

## Mô hình dữ liệu

Bảng `gold_price` gồm:

| Cột | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `gold_type` | VARCHAR(50) | Loại vàng |
| `buy_price` | DECIMAL(19,2) | Giá mua |
| `sell_price` | DECIMAL(19,2) | Giá bán |
| `updated_at` | TIMESTAMP | Thời điểm tạo/cập nhật gần nhất |

## Chạy ứng dụng

```powershell
cd task1
mvn spring-boot:run
```

Mặc định dữ liệu được lưu tại `task1/data/gold-price.mv.db`. Để dùng MySQL:

```powershell
$env:SPRING_PROFILES_ACTIVE="mysql"
$env:DB_URL="jdbc:mysql://localhost:3306/gold_price_db?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-password"
mvn spring-boot:run
```

Để dùng PostgreSQL, đổi profile thành `postgres` và đặt `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` tương ứng. Các tham số HikariCP có thể chỉnh bằng `DB_POOL_MAX_SIZE`, `DB_POOL_MIN_IDLE`, `DB_CONNECTION_TIMEOUT`, `DB_IDLE_TIMEOUT`, `DB_MAX_LIFETIME`.

## API

Base URL: `http://localhost:8080/api/gold-prices`

| Method | Endpoint | Chức năng |
|---|---|---|
| GET | `/health` | Kiểm tra dịch vụ |
| GET | `?goldType=SJC&page=0&size=10&sortBy=updatedAt&direction=desc` | Danh sách, tìm kiếm và phân trang |
| GET | `/{id}` | Chi tiết theo ID |
| POST | `/` | Thêm giá vàng |
| PUT | `/{id}` | Cập nhật giá vàng |
| DELETE | `/{id}` | Xóa giá vàng |

Các trường `sortBy` hợp lệ: `id`, `goldType`, `buyPrice`, `sellPrice`, `updatedAt`. `size` từ 1 đến 100.

Request dùng cho POST/PUT:

```json
{
  "goldType": "SJC",
  "buyPrice": 80000000,
  "sellPrice": 82000000
}
```

Response phân trang:

```json
{
  "content": [
    {
      "id": 1,
      "goldType": "SJC",
      "buyPrice": 80000000,
      "sellPrice": 82000000,
      "updatedAt": "2026-08-09T15:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

POST trả `201 Created`; DELETE trả `204 No Content`; dữ liệu không tồn tại trả `404`; request không hợp lệ trả `400` theo cấu trúc `ErrorResponse` thống nhất.

## Kiểm thử

```powershell
cd task1
mvn test
```

Bộ test bao phủ Repository với H2 in-memory, unit test Service bằng Mockito, đồng bộ dữ liệu, Scheduler và các luồng CRUD/tìm kiếm/phân trang/OpenAPI ở Controller.

## Đồng bộ giá vàng tự động

Scheduler mặc định tắt để ứng dụng không gọi nhầm một nguồn chưa được cấu hình. Bật bằng biến môi trường:

```powershell
$env:GOLD_PRICE_SCHEDULER_ENABLED="true"
$env:GOLD_PRICE_SOURCE_URL="https://your-provider.example/api/gold-prices"
$env:GOLD_PRICE_SOURCE_API_KEY="optional-api-key"
$env:GOLD_PRICE_SCHEDULER_CRON="0 */5 * * * *"
mvn spring-boot:run
```

Nguồn HTTP cần trả về một JSON array. Client chấp nhận cả tên trường camelCase và snake_case:

```json
[
  {
    "goldType": "SJC",
    "buyPrice": 80000000,
    "sellPrice": 82000000
  }
]
```

Các alias `gold_type`/`type`/`name`, `buy_price`/`buy`, `sell_price`/`sell` cũng được hỗ trợ. Bản ghi sai định dạng, giá bán thấp hơn giá mua hoặc trùng loại vàng và mức giá hiện có sẽ không được lưu. Cron mặc định chạy mỗi 5 phút theo múi giờ `Asia/Ho_Chi_Minh`.

## Redis Cache

Khi chạy cục bộ, cache dạng in-memory đơn giản được dùng để không bắt buộc cài Redis. Môi trường Docker đặt `CACHE_TYPE=redis`; hai cache `goldPrices` và `goldPriceById` có TTL mặc định 10 phút. POST, PUT, DELETE và lần đồng bộ có dữ liệu mới sẽ xóa cache liên quan.

## Swagger/OpenAPI

Sau khi chạy ứng dụng:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Docker Compose

Khởi chạy đồng thời ứng dụng, PostgreSQL và Redis:

```powershell
docker compose up --build
```

Muốn bật Scheduler trong Docker, thiết lập `GOLD_PRICE_SCHEDULER_ENABLED=true` và `GOLD_PRICE_SOURCE_URL` trước khi chạy lệnh trên. Dữ liệu PostgreSQL và Redis được giữ trong named volumes. Dừng hệ thống bằng `docker compose down`; chỉ thêm `-v` nếu thực sự muốn xóa dữ liệu.
