# Báo cáo kết quả Unit Test

**Lớp:** `AuthServiceImpl`
**Hàm:** `registerGuest(RegisterGuestRequest request)`

| Tên hàm | Mô tả trường hợp | Data test | Kết quả mong muốn | Kết quả thực tế (Pass/Fail) |
|---|---|---|---|---|
| `registerGuest_NewUser_Success` | Đăng ký tạo người dùng mới thành công (Happy Case) | `request={fullName: "Test Name", email: "test@email.com", password: "password"}` (email chưa tồn tại trong DB, `ROLE_USER` có sẵn) | Tạo mới User với `status=UNVERIFIED`, tạo Profile, tạo VerificationToken, gọi gửi email, trả về thông báo thành công. | Pass |
| `registerGuest_ExistingUnverifiedUser_Success` | Đăng ký lại với người dùng đã tồn tại nhưng chưa xác thực (`UNVERIFIED`) (Happy Case) | `request={fullName: "Test Name", email: "test@email.com", password: "password"}` (email đã có với `status=UNVERIFIED`, có Profile) | Cập nhật mật khẩu mới, cập nhật tên Profile, tạo VerificationToken mới, gọi gửi email, trả về thông báo thành công. | Pass |
| `registerGuest_EmailExistsAndActive_ThrowsException` | Đăng ký bằng email đã tồn tại và đã được kích hoạt (`ACTIVE`) (Negative Case) | `request={email: "test@email.com"}` (email có trạng thái `ACTIVE`) | Ném `AppException` với mã lỗi `EMAIL_EXISTED`. Không lưu vào DB, không gửi email. | Pass |
| `registerGuest_ExistingUnverifiedUser_ProfileNotFound_ThrowsException` | Đăng ký lại user `UNVERIFIED` nhưng bị mất dữ liệu `Profile` trong DB (Edge/Negative Case) | `request={email: "test@email.com"}` (user có trạng thái `UNVERIFIED`, không tìm thấy Profile theo `user id`) | Ném `AppException` với mã lỗi `UNCATEGORIZED_EXCEPTION`. Không tiến hành tạo mới token hay gửi email. | Pass |
| `registerGuest_NewUser_RoleNotFound_ThrowsException` | Đăng ký user mới nhưng hệ thống không có quyền `ROLE_USER` (Edge/Negative Case) | `request={email: "test@email.com"}` (email mới, DB không có `ROLE_USER`) | Ném `AppException` với mã lỗi `ROLE_NOT_FOUND`. Không tiến hành lưu user hay profile. | Pass |
