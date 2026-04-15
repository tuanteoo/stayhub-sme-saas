# Báo cáo kết quả Unit Test

**Lớp:** `PropertyServiceImpl`
**Hàm:** `createProperty(Long hostId, PropertyCreateRequest request)`

| Tên hàm | Mô tả trường hợp | Data test | Kết quả mong muốn | Kết quả thực tế (Pass/Fail) |
|---|---|---|---|---|
| `createProperty_Success` | Tạo mới Property thành công. Thông tin thỏa mãn đầy đủ các quy định về Role, gói đăng ký, chính sách huỷ phòng. (Happy Case) | `hostId=1`, có gói `ACTIVE` (giới hạn=5). `Deposit=50%`, `rooms=1`. | Lưu thành công Property xuống database (gọi `propertyRepository.save`). Không ném lỗi. | Pass |
| `createProperty_HostNotApproved_ThrowsException` | Tạo Property khi Host bị từ chối (`REJECTED`). Tính năng chỉ dành cho User có trạng thái `APPROVED` hoặc `PENDING`. (Edge Case/Negative Case) | `hostId=1`, `hostDetail` trạng thái `REJECTED`. | Ném ra `AppException(ErrorCode.HOST_NOT_APPROVED)`. Hủy tác vụ. | Pass |
| `createProperty_ListingLimitReached_ThrowsException` | Bị chặn tạo nhà do số Property đã đạt tối đa của gói cước (Ngăn User tạo vượt giới hạn - Negative case). | Gói `ACTIVE` `currentMaxListings=2`, số bài đăng hiện tại đã là 2. | Ném `InvalidDataException` với thông báo "đạt giới hạn tạo tối đa". Không gọi repo save. | Pass |
| `createProperty_InvalidDeposit_ThrowsException` | Mức tính tiền cộc (`DepositPercentage`) không thỏa mãn. Ví dụ: Chính sách hoàn tiền 50% nhưng chỉ cọc 30% khi thanh toán tại check-in. (Negative - Business rules) | Chính sách `refundPercentage=50`, tức tối thiểu cọc `50%`. Nhưng Request đưa vào `depositPercentage=30` (`isPayAtCheckinAllowed=true`). | Nhận ra sai phạm dòng tiền. Ném lỗi `InvalidDataException` với nội dung yêu cầu mức cọc hợp lệ. Hủy lưu database. | Pass |
