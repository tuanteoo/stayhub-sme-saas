# StayHub Backend

**StayHub** - Nền tảng kết nối và quản lý dịch vụ lưu trú Homestay.  
Đây là backend cho hệ thống StayHub, được xây dựng dựa trên kiến trúc module hóa với các chức năng chính bao gồm quản lý người dùng, chỗ ở, đặt phòng, thanh toán, và đánh giá.

## 🚀 Công nghệ sử dụng
- **Ngôn ngữ:** Java 17
- **Framework:** Spring Boot 3.5.11 (Spring Security, Spring Data JPA, Spring Web)
- **Cơ sở dữ liệu:** PostgreSQL
- **Database Migration:** Flyway
- **Authentication:** JSON Web Token (JWT)
- **CI/CD:** GitHub Actions (tự động build và test khi có commit mới)
- **Cloud:** AWS EC2, AWS S3 (lưu trữ hình ảnh), AWS RDS (cơ sở dữ liệu)
- **Network Globalization:** Cloudflare (DNS, SSL)
- **Thanh toán:** Tích hợp VNPay Sandbox
- **Template Engine:** Thymeleaf (cho Email templates)

## 🧩 Cấu trúc Module
Dự án được chia thành các hệ thống module chính như sau:
- `Module.Identity`: Quản lý người dùng (Host, Guest, Admin), xác thực và phân quyền truy cập.
- `Module.Property`: Quản lý danh sách, chi tiết và trạng thái của các homestay/chỗ ở.
- `Module.Booking`: Xử lý logic đặt phòng, huỷ phòng, và quản lý lịch trình dịch vụ.
- `Module.Finance`: Quản lý các giao dịch thanh toán (tích hợp VNPay) và thống kê thu nhập.
- `Module.Review`: Hệ thống đánh giá và nhận xét (Review & Rating) cho các chỗ ở.

## 🛠 Yêu cầu hệ thống (Prerequisites)
- [Java Development Kit (JDK) 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [Maven](https://maven.apache.org/) (hoặc sử dụng Maven Wrapper đi kèm dự án)
- Một hệ quản trị cơ sở dữ liệu [PostgreSQL](https://www.postgresql.org/) chạy trên môi trường local hoặc server của bạn.

## 📥 Khởi chạy dự án

### 1. Cấu hình cơ sở dữ liệu và biến môi trường
Mở thư mục `src/main/resources` và file `application.yaml` (hoặc `application-dev.yaml`) để chỉnh sửa thông tin kết nối Database, AWS S3, VNPay và JWT Secret:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/stayhub_db
    username: your_db_username
    password: your_db_password
```

Flyway sẽ tự động chạy các script migration trong thư mục `src/main/resources/db/migration` khi ứng dụng khởi động.

### 2. Biên dịch và Chạy ứng dụng

Sử dụng Maven Wrapper (ưu tiên) hoặc Maven đã cài đặt trên máy:
```bash
# Build dự án
./mvnw clean install

# Chạy ứng dụng
./mvnw spring-boot:run
```
Ứng dụng sẽ chạy ở cổng định sẵn, thông thường là `http://localhost:8080`.

## 🧪 Testing
Để chạy test case cho các tính năng:
```bash
./mvnw test
```
Báo cáo Unit Test và coverage sẽ được sinh ra ở thư mục `target/site/`.

---
*Dự án thuộc Đồ án Tốt nghiệp.*
