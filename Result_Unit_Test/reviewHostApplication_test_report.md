# Báo cáo kết quả Unit Test

**Lớp:** `HostOnboardingServiceImpl`
**Hàm:** `reviewHostApplication(String hostCode, HostApprovalRequest request)`

| Tên hàm | Mô tả trường hợp | Data test | Kết quả mong muốn | Kết quả thực tế (Pass/Fail) |
|---|---|---|---|---|
| `reviewHostApplication_HostNotFound_ThrowsException` | Không tìm thấy hồ sơ đăng ký host theo `hostCode` | `hostCode="HOU-XYZ"`, `request={status: REJECTED}` | Ném `ResourceNotFoundException("Không tìm thấy hồ sơ đăng ký host!")` | Pass |
| `reviewHostApplication_StatusRejected_UpdatesStatusAndNote` | Host bị từ chối (`REJECTED`) | `hostCode="HOU-ABC-DEF-GHI"`, `request={status: REJECTED, reviewNote: "Invalid documents"}` | Cập nhật status thành `REJECTED`, lưu note. Không gọi các module khác. | Pass |
| `reviewHostApplication_StatusRequestChanges_UpdatesStatusAndNote` | Yêu cầu host cập nhật thông tin (`REQUEST_CHANGES`) | `hostCode="HOU-ABC-DEF-GHI"`, `request={status: REQUEST_CHANGES, reviewNote: "Please update ID front"}` | Cập nhật status thành `REQUEST_CHANGES`, lưu note. Không gọi các module khác. | Pass |
| `reviewHostApplication_StatusApproved_RoleNotFound_ThrowsException` | `APPROVED` nhưng không tìm thấy `ROLE_HOST` trong cơ sở dữ liệu | `hostCode="HOU-ABC-DEF-GHI"`, `request={status: APPROVED}` | Ném `ResourceNotFoundException("Lỗi hệ thống: Không tìm thấy quyền hợp lệ")` | Pass |
| `reviewHostApplication_StatusApproved_PlanNotFound_ThrowsException` | `APPROVED` nhưng không tìm thấy `SubscriptionPlan` loại `FREE` | `hostCode="HOU-ABC-DEF-GHI"`, `request={status: APPROVED}`, DB có `ROLE_HOST` | Ném `ResourceNotFoundException("Không tìm thấy gói cước FREE cấu hình trong hệ thống")` | Pass |
| `reviewHostApplication_StatusApproved_Success` | `APPROVED` thành công, các dữ liệu liên quan đầy đủ | `hostCode="HOU-ABC-DEF-GHI"`, `request={status: APPROVED}` | Duyệt tài khoản, thêm `ROLE_HOST`, gán gói cước `FREE`, gửi email, duyệt property. | Pass |
