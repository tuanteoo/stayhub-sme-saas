<div align="center">
  <h1>🏨 StayHub Backend</h1>
  <p><strong>Nền tảng kết nối và quản lý dịch vụ lưu trú Homestay</strong></p>
  
  <p>
    <img src="https://img.shields.io/badge/Java-17-orange.svg" alt="Java 17" />
    <img src="https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen.svg" alt="Spring Boot 3.5.11" />
    <img src="https://img.shields.io/badge/PostgreSQL-14%2B-blue.svg" alt="PostgreSQL" />
    <img src="https://img.shields.io/badge/Flyway-Database%20Migration-red.svg" alt="Flyway" />
  </p>
</div>

---

## 📖 Giới thiệu
Đây là mã nguồn Backend cho hệ thống **StayHub**, được xây dựng theo kiến trúc **Modular Monolith** nhằm đảm bảo tính mở rộng và dễ bảo trì. Ứng dụng cung cấp các API xử lý toàn bộ nghiệp vụ lõi: quản lý người dùng, chỗ ở, đặt phòng, thanh toán, và đánh giá.

---

## 🚀 Công nghệ & Công cụ (Tech Stack)
- **Ngôn ngữ:** Java 17
- **Core Framework:** Spring Boot 3.5.11 (Spring Security, Spring Data JPA, Spring Web)
- **Cơ sở dữ liệu:** PostgreSQL
- **Database Migration:** Flyway
- **Authentication/Authorization:** JSON Web Token (JWT)
- **Cloud & DevOps:**
  - AWS EC2 (Hosting), AWS S3 (Lưu trữ ảnh qua Spring Cloud AWS), AWS RDS
  - Cloudflare (DNS, SSL)
  - GitHub Actions (CI/CD Pipeline)
- **Tích hợp bên thứ ba:**
  - **Thanh toán:** VNPay Sandbox
  - **Email Template:** Thymeleaf
- **API Documentation:** Springdoc OpenAPI (Swagger UI)
- **Testing & Quality:** JUnit 5, Jacoco, Diffblue Cover

---

## 🧩 Cấu trúc Kiến trúc (Module Structure)
Dự án được phân chia thành các module nghiệp vụ độc lập (`src/main/java/com/stayhub/backend/Module/`):

- 🔐 **`Module.Identity`**: Quản lý tài khoản (Host, Guest, Admin), xác thực JWT, phân quyền truy cập.
- 🏠 **`Module.Property`**: Quản lý thông tin, chi tiết và trạng thái phòng/homestay.
- 📅 **`Module.Booking`**: Logic đặt phòng, kiểm tra tình trạng trống, lịch trình.
- 💳 **`Module.Finance`**: Tích hợp cổng thanh toán VNPay, ghi nhận giao dịch, thống kê doanh thu.
- ⭐ **`Module.Review`**: Hệ thống xếp hạng, đánh giá từ khách hàng cho chỗ ở.

---

## 🛠 Yêu cầu hệ thống (Prerequisites)
Để chạy dự án local, máy tính của bạn cần cài đặt:
- [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [Maven](https://maven.apache.org/) (tùy chọn, dự án đã có sẵn Maven Wrapper)
- [PostgreSQL](https://www.postgresql.org/) chạy ở cổng `5432`

---

## 📥 Hướng dẫn cài đặt & Khởi chạy

### 1. Cấu hình Database & Biến môi trường
Mở thư mục `src/main/resources` và sao chép/chỉnh sửa file cấu hình `application.yaml` (hoặc `application-dev.yaml`). Cập nhật các thông số kết nối Database, AWS S3, VNPay và JWT Secret:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/stayhub_db
    username: <your_db_username>
    password: <your_db_password>
```
*Lưu ý: Flyway sẽ tự động chạy các script migration (`src/main/resources/db/migration`) khi ứng dụng khởi động lần đầu để tạo các bảng cần thiết.*

### 2. Build & Chạy ứng dụng
Sử dụng script **Maven Wrapper** có sẵn trong dự án:

**Trên Linux/macOS:**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

**Trên Windows (PowerShell/CMD):**
```cmd
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

Ứng dụng sẽ khởi chạy và lắng nghe ở cổng mặc định: `http://localhost:8080`.

---

## 🐳 CI/CD & Docker Deployment

Dự án đã được thiết lập sẵn quy trình **CI/CD hoàn chỉnh** sử dụng GitHub Actions và kiến trúc **Blue-Green Deployment** qua Docker Compose để đảm bảo ứng dụng cập nhật không bị gián đoạn (zero-downtime).

### 1. Kiến trúc Triển khai (Deployment Architecture)
- **GitHub Actions (`.github/workflows/deploy-develop.yaml`)**: Tự động lắng nghe sự kiện `push` trên nhánh `develop`.
- **Docker Hub**: Build và push Docker image lên repository tự động.
- **Docker Compose (`docker-compose.yml`)**: Quản lý các container theo mô hình Blue-Green:
  - `app_blue` (chạy ở cổng 8080)
  - `app_green` (chạy ở cổng 8081)
  - `nginx` proxy điều hướng lưu lượng giữa các môi trường ở cổng 80.

### 2. Chạy ứng dụng nội bộ bằng Docker (Local)
Nếu bạn đã cài đặt [Docker](https://www.docker.com/) và [Docker Compose](https://docs.docker.com/compose/), bạn có thể khởi chạy toàn bộ hệ thống (bao gồm ứng dụng và Nginx) dễ dàng:

1. Tạo file `.env` ở thư mục gốc (root directory) để cung cấp các thông số bắt buộc, ví dụ:
```env
DOCKER_USERNAME=your_docker_username
```

2. Sử dụng câu lệnh sau để build và chạy ứng dụng:
```bash
# Khởi chạy ngầm các container
docker compose up -d
```

3. Bạn có thể truy cập hệ thống ở `http://localhost` (thông qua Nginx proxy). Để dừng hệ thống:
```bash
docker compose down
```

---

## 📚 API Documentation (Swagger)
Dự án sử dụng **Springdoc OpenAPI** để tự động tạo tài liệu API. Sau khi khởi động ứng dụng, bạn có thể truy cập tài liệu qua trình duyệt:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## 🧪 Testing & Code Coverage
Dự án đi kèm với các bộ test (Unit Test) và công cụ đo lường coverage:
```bash
./mvnw test
```
- Báo cáo kết quả test sẽ nằm ở `target/surefire-reports/`.
- Báo cáo **Jacoco Coverage** sẽ được sinh ra dưới dạng HTML tại: `target/site/jacoco/index.html`.

---

<div align="center">
  <i>Đồ án Tốt nghiệp</i>
</div>
