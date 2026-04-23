# Báo cáo kết quả Unit Test

**Lớp:** `AuthServiceImpl`
**Hàm:** `login(LoginRequest request)`

| Tên hàm | Mô tả trường hợp | Data test | Kết quả mong muốn | Kết quả thực tế (Pass/Fail) |
|---|---|---|---|---|
| `login_Success` | Đăng nhập thành công (Happy Case) | `request={email: "test@email.com", password: "password"}` (tài khoản hợp lệ, profile tồn tại) | Xác thực thành công, lấy được Profile. Trả về `LoginResponse` chứa AccessToken, RefreshToken đúng chuẩn cùng thông tin cơ bản của User. Gọi hàm `save` vào `refreshTokenRepository` và update `lastLoginAt`. | Pass |
| `login_BadCredentials_ThrowsException` | Đăng nhập thất bại do sai mật khẩu (Negative Case) | `request={email: "test@email.com", password: "wrongpassword"}` | `AuthenticationManager` ném lỗi `BadCredentialsException`. Không sinh Token và không gọi repository lưu thông tin. | Pass |
| `login_ProfileNotFound_SuccessWithEmptyProfile` | Đăng nhập thành công nhưng DB bị mất dòng Profile tương ứng của tài khoản này (Edge Case) | `request={email: "test@email.com", password: "password"}` (tài khoản hợp lệ, nhưng `profileRepository.findByUserId` trả về rỗng) | Vẫn xử lý đăng nhập thành công. Tạo 1 đối tượng `Profile` rỗng tạm thời, trả về thông tin tên và avatar là null. Không làm gián đoạn việc đăng nhập. | Pass |
