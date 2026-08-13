# Tính năng và API của Gold Price API

Tài liệu này mô tả toàn bộ tính năng, endpoint, dữ liệu đầu vào/đầu ra và các trường hợp kiểm thử của dự án.

## 1. Tổng quan

- Base URL: `http://localhost:8080`
- Base API: `http://localhost:8080/api/gold-prices`
- Content-Type cho POST/PUT: `application/json`
- Database ban đầu có thể rỗng vì dự án không tự chèn dữ liệu giả.
- Để thử các API đọc, hãy gọi POST tạo dữ liệu trước hoặc bật Scheduler với nguồn giá thật.
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 2. Danh sách tính năng

| Nhóm | Tính năng |
|---|---|
| REST API | Health check, tạo, đọc, cập nhật và xóa giá vàng |
| Tra cứu | Tìm kiếm gần đúng theo loại vàng, không phân biệt hoa/thường |
| Phân trang | Chọn trang, kích thước trang, trường và chiều sắp xếp |
| Database | JPA/Hibernate, HikariCP; hỗ trợ H2, PostgreSQL và MySQL |
| DTO | Request/response tách khỏi Entity bằng MapStruct |
| Validation | Kiểm tra loại vàng, giá, ID và tham số phân trang |
| Cache | Cache danh sách và chi tiết; tự xóa sau CRUD/đồng bộ |
| Scheduler | Tự gọi nguồn HTTP theo cron và lưu dữ liệu mới |
| Chống trùng | Không lưu lại cùng loại vàng, giá mua và giá bán |
| OpenAPI | Swagger UI và tài liệu JSON tự động |
| Exception | Global Exception Handling trả JSON thống nhất |
| Logging | Request ID, console log, rolling file và log Scheduler |
| Kiểm thử | JUnit, Mockito, MockMvc, `@DataJpaTest`, Postman |
| Đóng gói | Maven executable JAR, Dockerfile và Docker Compose |

## 3. Cấu trúc dữ liệu

### GoldPriceRequest

Được dùng cho POST và PUT:

```json
{
  "goldType": "SJC",
  "buyPrice": 80000000,
  "sellPrice": 82000000
}
```

| Trường | Kiểu | Quy tắc |
|---|---|---|
| `goldType` | string | Bắt buộc, tối đa 50 ký tự; nhận chữ Unicode, số, dấu cách, `.`, `_`, `-` |
| `buyPrice` | decimal | Bắt buộc, không âm; tối đa 17 chữ số nguyên và 2 số thập phân |
| `sellPrice` | decimal | Bắt buộc, không âm; tối đa 17 chữ số nguyên và 2 số thập phân |

Quy tắc nghiệp vụ: `sellPrice` phải lớn hơn hoặc bằng `buyPrice`. `goldType` được trim và chuyển sang chữ hoa trước khi lưu.

### GoldPriceResponse

```json
{
  "id": 1,
  "goldType": "SJC",
  "buyPrice": 80000000.00,
  "sellPrice": 82000000.00,
  "updatedAt": "2026-08-13T09:30:15.123456"
}
```

`updatedAt` do server tự tạo khi thêm hoặc cập nhật, client không truyền trường này.

## 4. Chuẩn bị dữ liệu để thử API

Vì Database không có dữ liệu giả, chạy lần lượt các request sau.

### Tạo SJC

```http
POST /api/gold-prices
Content-Type: application/json

{
  "goldType": "SJC",
  "buyPrice": 80000000,
  "sellPrice": 82000000
}
```

### Tạo vàng 9999

```http
POST /api/gold-prices
Content-Type: application/json

{
  "goldType": "Vàng 9999",
  "buyPrice": 79000000,
  "sellPrice": 81000000
}
```

### Tạo vàng nhẫn SJC

```http
POST /api/gold-prices
Content-Type: application/json

{
  "goldType": "SJC Nhẫn",
  "buyPrice": 78500000,
  "sellPrice": 80500000
}
```

ID thực tế được Database sinh tự động. Hãy dùng ID trả về từ POST cho GET/PUT/DELETE thay vì luôn giả định ID bằng `1`.

## 5. Chi tiết tất cả API

### 5.1. Health check

```http
GET /api/gold-prices/health
```

Response — `200 OK`:

```json
{
  "status": "ok",
  "service": "gold-price-api",
  "checkedAt": "2026-08-13T02:30:15.123Z"
}
```

PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api/gold-prices/health
```

Lưu ý: endpoint này xác nhận ứng dụng HTTP đang chạy; hiện tại nó không thực hiện health check sâu tới Database và Redis.

### 5.2. Tạo giá vàng

```http
POST /api/gold-prices
Content-Type: application/json
```

Request:

```json
{
  "goldType": "sjc",
  "buyPrice": 80000000,
  "sellPrice": 82000000
}
```

Response — `201 Created`:

```http
Location: /api/gold-prices/1
X-Request-ID: 3ca95947-5ebe-42eb-b3e3-72bb2eab570f
```

```json
{
  "id": 1,
  "goldType": "SJC",
  "buyPrice": 80000000.00,
  "sellPrice": 82000000.00,
  "updatedAt": "2026-08-13T09:35:00.512341"
}
```

PowerShell:

```powershell
$body = @{
    goldType = "SJC"
    buyPrice = 80000000
    sellPrice = 82000000
} | ConvertTo-Json

$created = Invoke-RestMethod -Method Post `
    -Uri http://localhost:8080/api/gold-prices `
    -ContentType "application/json" -Body $body

$id = $created.id
```

Tác động cache: xóa toàn bộ cache danh sách và cache chi tiết.

### 5.3. Lấy danh sách mặc định

```http
GET /api/gold-prices
```

Tương đương:

```http
GET /api/gold-prices?page=0&size=10&sortBy=updatedAt&direction=desc
```

Response — `200 OK`:

```json
{
  "content": [
    {
      "id": 3,
      "goldType": "SJC NHẪN",
      "buyPrice": 78500000.00,
      "sellPrice": 80500000.00,
      "updatedAt": "2026-08-13T09:37:00.000000"
    },
    {
      "id": 2,
      "goldType": "VÀNG 9999",
      "buyPrice": 79000000.00,
      "sellPrice": 81000000.00,
      "updatedAt": "2026-08-13T09:36:00.000000"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 3,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

Khi Database rỗng, API vẫn trả `200 OK`:

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

### 5.4. Tìm kiếm, phân trang và sắp xếp

```http
GET /api/gold-prices?goldType=sjc&page=0&size=2&sortBy=buyPrice&direction=asc
```

Response ví dụ — `200 OK`:

```json
{
  "content": [
    {
      "id": 3,
      "goldType": "SJC NHẪN",
      "buyPrice": 78500000.00,
      "sellPrice": 80500000.00,
      "updatedAt": "2026-08-13T09:37:00.000000"
    },
    {
      "id": 1,
      "goldType": "SJC",
      "buyPrice": 80000000.00,
      "sellPrice": 82000000.00,
      "updatedAt": "2026-08-13T09:35:00.000000"
    }
  ],
  "page": 0,
  "size": 2,
  "totalElements": 2,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

Tham số:

| Tham số | Bắt buộc | Mặc định | Quy tắc |
|---|---|---|---|
| `goldType` | Không | Không lọc | Tìm chuỗi chứa, không phân biệt hoa/thường |
| `page` | Không | `0` | Từ 0 trở lên |
| `size` | Không | `10` | Từ 1 đến 100 |
| `sortBy` | Không | `updatedAt` | `id`, `goldType`, `buyPrice`, `sellPrice`, `updatedAt` |
| `direction` | Không | `desc` | `asc` hoặc `desc`, không phân biệt hoa/thường |

Các request ví dụ:

```http
GET /api/gold-prices?goldType=SJC
GET /api/gold-prices?page=1&size=5
GET /api/gold-prices?sortBy=goldType&direction=asc
GET /api/gold-prices?goldType=9999&sortBy=sellPrice&direction=desc
```

Kết quả tìm kiếm được cache theo toàn bộ bộ tham số: `goldType`, `page`, `size` và sort.

### 5.5. Lấy chi tiết theo ID

```http
GET /api/gold-prices/1
```

Response — `200 OK`:

```json
{
  "id": 1,
  "goldType": "SJC",
  "buyPrice": 80000000.00,
  "sellPrice": 82000000.00,
  "updatedAt": "2026-08-13T09:35:00.512341"
}
```

Không tồn tại — `404 Not Found`:

```json
{
  "status": 404,
  "error": "DATA_NOT_FOUND",
  "message": "Không tìm thấy giá vàng có id: 999999999",
  "path": "/api/gold-prices/999999999",
  "requestId": "c9baf1b9-a0a4-4fd4-b3e8-8e358048fb5a",
  "timestamp": "2026-08-13T02:40:00.000Z"
}
```

Kết quả hợp lệ được cache theo ID.

### 5.6. Cập nhật giá vàng

```http
PUT /api/gold-prices/1
Content-Type: application/json
```

Request:

```json
{
  "goldType": "SJC",
  "buyPrice": 81000000,
  "sellPrice": 83000000
}
```

Response — `200 OK`:

```json
{
  "id": 1,
  "goldType": "SJC",
  "buyPrice": 81000000.00,
  "sellPrice": 83000000.00,
  "updatedAt": "2026-08-13T09:45:25.983210"
}
```

`updatedAt` được cập nhật lại. Nếu ID không tồn tại, API trả `404 DATA_NOT_FOUND`. Sau cập nhật, cache danh sách và chi tiết đều bị xóa.

PowerShell:

```powershell
$body = @{
    goldType = "SJC"
    buyPrice = 81000000
    sellPrice = 83000000
} | ConvertTo-Json

Invoke-RestMethod -Method Put `
    -Uri "http://localhost:8080/api/gold-prices/$id" `
    -ContentType "application/json" -Body $body
```

### 5.7. Xóa giá vàng

```http
DELETE /api/gold-prices/1
```

Response thành công — `204 No Content`, không có JSON body.

PowerShell:

```powershell
Invoke-RestMethod -Method Delete `
    -Uri "http://localhost:8080/api/gold-prices/$id"
```

Nếu ID không tồn tại, API trả `404 DATA_NOT_FOUND`. Sau khi xóa, cache danh sách và chi tiết đều bị xóa.

### 5.8. OpenAPI JSON

```http
GET /v3/api-docs
```

Response — `200 OK`, nội dung là tài liệu OpenAPI 3 JSON. Ví dụ rút gọn:

```json
{
  "openapi": "3.0.1",
  "info": {
    "title": "Gold Price API",
    "description": "API quản lý, tra cứu và tự động đồng bộ giá vàng",
    "version": "1.0.0"
  },
  "paths": {
    "/api/gold-prices": {}
  }
}
```

### 5.9. Swagger UI

```http
GET /swagger-ui.html
```

Trình duyệt được chuyển tới giao diện Swagger UI, cho phép gọi các API mà không cần Postman.

## 6. Các trường hợp lỗi cần kiểm thử

Format lỗi chung:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Dữ liệu đầu vào không hợp lệ",
  "path": "/api/gold-prices",
  "requestId": "c2297303-274c-4e99-a5c1-b5cf2925af9c",
  "timestamp": "2026-08-13T02:50:00.000Z",
  "fieldErrors": {
    "goldType": "Loại vàng không được để trống"
  }
}
```

`fieldErrors` chỉ xuất hiện khi có lỗi validation theo trường.

### Body thiếu hoặc sai dữ liệu

```http
POST /api/gold-prices
Content-Type: application/json

{
  "goldType": "",
  "buyPrice": -1,
  "sellPrice": null
}
```

Kết quả: `400 VALIDATION_ERROR` với `fieldErrors` cho ba trường.

### Giá bán thấp hơn giá mua

```json
{
  "goldType": "SJC",
  "buyPrice": 82000000,
  "sellPrice": 80000000
}
```

Kết quả — `400 Bad Request`:

```json
{
  "status": 400,
  "error": "BUSINESS_RULE_VIOLATION",
  "message": "Giá bán phải lớn hơn hoặc bằng giá mua",
  "path": "/api/gold-prices",
  "requestId": "9a195f14-5f15-4465-978f-5ae43c959e76",
  "timestamp": "2026-08-13T02:51:00.000Z"
}
```

### JSON sai cú pháp

```http
POST /api/gold-prices
Content-Type: application/json

{not-json}
```

Kết quả: `400 MALFORMED_JSON`, message `Nội dung JSON không hợp lệ`.

### ID sai kiểu

```http
GET /api/gold-prices/abc
```

Kết quả: `400 INVALID_PARAMETER`, message `Tham số 'id' không đúng kiểu dữ liệu`.

### ID bằng 0 hoặc âm

```http
GET /api/gold-prices/0
```

Kết quả: `400 VALIDATION_ERROR` vì ID phải từ 1 trở lên.

### Kích thước trang không hợp lệ

```http
GET /api/gold-prices?size=101
```

Kết quả: `400 VALIDATION_ERROR`.

### Trường sắp xếp không hợp lệ

```http
GET /api/gold-prices?sortBy=unknown
```

Kết quả: `400 VALIDATION_ERROR`, `fieldErrors.sortBy` có message `Trường sắp xếp không hợp lệ`.

### Chiều sắp xếp không hợp lệ

```http
GET /api/gold-prices?direction=random
```

Kết quả: `400 VALIDATION_ERROR`, `fieldErrors.direction` có message `Chiều sắp xếp phải là asc hoặc desc`.

### Sai Content-Type

```http
POST /api/gold-prices
Content-Type: text/plain
```

Kết quả: `415 UNSUPPORTED_MEDIA_TYPE`.

### Sai phương thức HTTP

```http
PATCH /api/gold-prices/1
```

Kết quả: `405 METHOD_NOT_ALLOWED`.

### Endpoint không tồn tại

```http
GET /api/does-not-exist
```

Kết quả: `404 ENDPOINT_NOT_FOUND`.

### Database hoặc Redis không hoạt động

- Lỗi Database được ánh xạ thành `503 DATABASE_UNAVAILABLE`.
- Lỗi kết nối Redis được ánh xạ thành `503 CACHE_UNAVAILABLE`.
- Chi tiết kỹ thuật và stack trace chỉ được ghi trong server log, không trả cho client.

## 7. Request ID và logging

Mỗi response có header:

```http
X-Request-ID: 3ca95947-5ebe-42eb-b3e3-72bb2eab570f
```

Client có thể tự truyền ID hợp lệ:

```http
X-Request-ID: postman-test-001
```

Server giữ ID này trong response và log. ID chỉ nhận chữ, số, `.`, `_`, `-`, tối đa 64 ký tự; nếu không hợp lệ server tự sinh UUID.

Log được ghi:

- Console.
- `task1/logs/gold-price-api.log` khi chạy từ thư mục `task1`.
- Docker volume `app_logs` khi chạy Docker Compose.
- File xoay theo ngày hoặc khi đạt 10 MB; giữ tối đa 14 ngày.

## 8. Redis Cache

Hai vùng cache:

| Cache | Dữ liệu |
|---|---|
| `goldPrices` | Kết quả tìm kiếm/phân trang |
| `goldPriceById` | Chi tiết theo ID |

TTL mặc định: 10 phút. POST, PUT, DELETE và Scheduler lưu được bản ghi mới sẽ xóa cache để API không trả dữ liệu cũ.

Local mặc định dùng `CACHE_TYPE=simple`, không cần cài Redis. Docker Compose dùng Redis thật với `CACHE_TYPE=redis`.

## 9. Scheduler đồng bộ tự động

Mặc định Scheduler tắt. Cấu hình để bật:

```powershell
$env:GOLD_PRICE_SCHEDULER_ENABLED="true"
$env:GOLD_PRICE_SOURCE_URL="https://provider.example/api/gold-prices"
$env:GOLD_PRICE_SOURCE_API_KEY="optional-key"
$env:GOLD_PRICE_SCHEDULER_CRON="0 */5 * * * *"
```

Cron mặc định chạy mỗi 5 phút theo múi giờ `Asia/Ho_Chi_Minh`.

Nguồn hợp lệ:

```json
[
  {
    "goldType": "SJC",
    "buyPrice": 80000000,
    "sellPrice": 82000000
  },
  {
    "gold_type": "Vàng 9999",
    "buy_price": 79000000,
    "sell_price": 81000000
  }
]
```

Các tên thay thế được chấp nhận:

- Loại vàng: `goldType`, `gold_type`, `type`, `name`.
- Giá mua: `buyPrice`, `buy_price`, `buy`.
- Giá bán: `sellPrice`, `sell_price`, `sell`.

Scheduler thực hiện:

1. Gọi nguồn bằng WebClient.
2. Lọc bản ghi thiếu/sai, giá âm hoặc giá bán thấp hơn giá mua.
3. Chuẩn hóa loại vàng thành chữ hoa.
4. Loại bản ghi trùng trong cùng response.
5. Kiểm tra bản ghi cùng loại vàng, giá mua và giá bán đã có trong Database.
6. Chỉ lưu bản ghi mới và xóa cache nếu có thay đổi.
7. Ghi log số bản ghi nhận, lưu và thời gian xử lý.

Không có REST endpoint để kích hoạt Scheduler thủ công; job chạy theo cron sau khi được bật.

## 10. Database và lưu trữ

Bảng `gold_price`:

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT, identity | Khóa chính tự tăng |
| `gold_type` | VARCHAR(50) | Loại vàng |
| `buy_price` | DECIMAL(19,2) | Giá mua |
| `sell_price` | DECIMAL(19,2) | Giá bán |
| `updated_at` | TIMESTAMP | Thời gian tạo/cập nhật |

Các chế độ:

- Mặc định: H2 file, không cần cài DB ngoài.
- Profile `postgres`: PostgreSQL.
- Profile `mysql`: MySQL.
- Docker Compose: PostgreSQL + Redis + application.

## 11. Test nhanh toàn bộ luồng CRUD bằng PowerShell

```powershell
$baseUrl = "http://localhost:8080/api/gold-prices"

# 1. Health
Invoke-RestMethod "$baseUrl/health"

# 2. Create
$createBody = @{
    goldType = "SJC"
    buyPrice = 80000000
    sellPrice = 82000000
} | ConvertTo-Json
$created = Invoke-RestMethod -Method Post -Uri $baseUrl `
    -ContentType "application/json" -Body $createBody
$id = $created.id

# 3. List/search
Invoke-RestMethod "$baseUrl?goldType=SJC&page=0&size=10&sortBy=updatedAt&direction=desc"

# 4. Detail
Invoke-RestMethod "$baseUrl/$id"

# 5. Update
$updateBody = @{
    goldType = "SJC"
    buyPrice = 81000000
    sellPrice = 83000000
} | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri "$baseUrl/$id" `
    -ContentType "application/json" -Body $updateBody

# 6. Delete
Invoke-RestMethod -Method Delete -Uri "$baseUrl/$id"
```

## 12. Công cụ kiểm thử có sẵn

- Swagger UI: `/swagger-ui.html`.
- OpenAPI JSON: `/v3/api-docs`.
- Postman Collection: `postman/Gold-Price-API-Week5.postman_collection.json`.
- Test tự động: chạy `mvn clean test` trong thư mục `task1`.
- Smoke test JAR: chạy `powershell -ExecutionPolicy Bypass -File scripts/smoke-test-jar.ps1` tại thư mục gốc.

Các JSON phía trên là dữ liệu minh họa. `id`, `updatedAt`, `requestId` và tổng số bản ghi sẽ thay đổi theo lần chạy thực tế.
