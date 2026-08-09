# Gold Price API

REST API quản lý giá vàng được xây dựng bằng Java 17, Spring Boot, Spring Data JPA và HikariCP. Dữ liệu được lưu bền vững thay cho mock data; API hỗ trợ CRUD, tìm kiếm, phân trang và sắp xếp.

## Công nghệ

- Java 17, Spring Boot 3.3
- Spring Web, Validation, Data JPA
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

Bộ test bao phủ Repository với H2 in-memory, nghiệp vụ Service và các luồng CRUD/tìm kiếm/phân trang ở Controller.
