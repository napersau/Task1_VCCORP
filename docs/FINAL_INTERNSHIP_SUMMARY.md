# Tổng kết kỳ thực tập

## Thông tin

- Sinh viên: Nguyễn Đức Khởi
- Mã sinh viên: B22DCCN470
- Đơn vị thực tập: Công ty Cổ phần VCCORP
- Dự án: Gold Price API

## Kết quả theo giai đoạn

1. Xây dựng REST API tra cứu giá vàng, DTO, validation và cấu trúc Controller–Service–Repository.
2. Chuyển mock data sang JPA và cơ sở dữ liệu bền vững; hoàn thiện CRUD, tìm kiếm, phân trang và integration test.
3. Bổ sung Scheduler/WebClient để đồng bộ nguồn ngoài, MapStruct, Redis Cache, Swagger/OpenAPI và Docker Compose.
4. Chuẩn hóa Global Exception Handling, request ID, Logback rolling file, unit test Mockito và Postman Collection.
5. Hoàn thiện tài liệu cài đặt, kiến trúc, biến môi trường, đóng gói JAR và quy trình bàn giao.

## Kiến thức đạt được

- Thiết kế REST API và phân tầng ứng dụng Spring Boot.
- Spring Data JPA, transaction, derived query, phân trang và HikariCP.
- DTO Pattern, validation và MapStruct.
- Scheduler, cron expression và giao tiếp HTTP bằng WebClient.
- Spring Cache, Redis, cache key, TTL và cache invalidation.
- Global Exception Handling, logging có request ID và bảo vệ thông tin lỗi nội bộ.
- Unit test, integration test, MockMvc, Mockito, Postman và Swagger.
- Maven packaging, Dockerfile multi-stage và Docker Compose.

## Kết quả bàn giao

- Source code có test tự động và tài liệu API.
- File JAR chạy độc lập bằng Java 17.
- Môi trường Docker gồm ứng dụng, PostgreSQL và Redis.
- Postman Collection kiểm tra luồng thành công và lỗi.
- README, tài liệu kiến trúc và mẫu biến môi trường.

## Hướng phát triển

- Bổ sung Flyway/Liquibase để quản lý schema theo phiên bản.
- Thêm authentication/authorization nếu API không còn public.
- Tích hợp metrics, tracing và dashboard giám sát.
- Thay nguồn demo bằng nhà cung cấp dữ liệu chính thức có SLA.
- Thêm Testcontainers cho kiểm thử PostgreSQL và Redis thực tế trong CI.
