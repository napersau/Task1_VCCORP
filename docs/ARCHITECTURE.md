# Kiến trúc Gold Price API

## Luồng xử lý API

```text
HTTP Client
    │
    ▼
RequestIdFilter ── tạo/nhận X-Request-ID và đưa vào MDC
    │
    ▼
GoldPriceController ── validation request
    │
    ▼
GoldPriceService ── transaction, cache, nghiệp vụ
    │                    │
    ▼                    ▼
GoldPriceRepository    Redis Cache
    │
    ▼
PostgreSQL / MySQL / H2
```

Entity chỉ được sử dụng trong tầng dữ liệu. MapStruct chuyển `GoldPrice` sang `GoldPriceResponse`; client gửi dữ liệu qua `GoldPriceRequest`.

## Luồng đồng bộ tự động

```text
Cron → GoldPriceScheduler → GoldPriceSourceClient (WebClient)
                           → GoldPriceSynchronizationService
                           → lọc dữ liệu lỗi/trùng
                           → JPA Repository → Database
                           → xóa cache nếu có dữ liệu mới
```

Scheduler mặc định tắt. Khi một lần chạy lỗi, exception được ghi log và không thoát khỏi hàm scheduled, vì vậy lần chạy kế tiếp vẫn được thực hiện.

## Cache

- `goldPrices`: cache kết quả tìm kiếm theo loại vàng, trang, kích thước và sắp xếp.
- `goldPriceById`: cache chi tiết theo ID.
- POST, PUT, DELETE và đồng bộ thành công sẽ xóa cache liên quan.
- Local mặc định dùng cache đơn giản; Docker dùng Redis với TTL 10 phút.

## Xử lý lỗi

`GlobalExceptionHandler` chuyển exception sang response JSON thống nhất. Lỗi hạ tầng được ghi đầy đủ trong server log nhưng client chỉ nhận thông điệp an toàn. `requestId` trong response và log giúp truy vết cùng một yêu cầu.

## Quyết định vận hành

- Không lưu mật khẩu hoặc API key trong source code.
- Scheduler không bật nếu chưa có URL nguồn thật.
- Log xoay theo ngày/kích thước và được lưu trong Docker volume.
- HikariCP giới hạn pool và timeout qua biến môi trường.
- H2 file phù hợp demo local; PostgreSQL/MySQL phù hợp môi trường tích hợp.
