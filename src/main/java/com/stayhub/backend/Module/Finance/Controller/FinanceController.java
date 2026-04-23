package com.stayhub.backend.Module.Finance.Controller;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Finance.DTO.Request.BankAccountRequest;
import com.stayhub.backend.Module.Finance.DTO.Request.PayoutProcessRequest;
import com.stayhub.backend.Module.Finance.DTO.Request.PayoutVerifyRequest;
import com.stayhub.backend.Module.Finance.DTO.Response.BankAccountResponse;
import com.stayhub.backend.Module.Finance.DTO.Request.PayoutCreateRequest;
import com.stayhub.backend.Module.Finance.DTO.Response.PayoutResponse;
import com.stayhub.backend.Module.Finance.DTO.Response.TransactionResponse;
import com.stayhub.backend.Module.Finance.DTO.Response.WalletResponse;
import com.stayhub.backend.Module.Finance.Service.BankAccountService;
import com.stayhub.backend.Module.Finance.Service.PaymentService;
import com.stayhub.backend.Module.Finance.Service.WalletService;
import com.stayhub.backend.Module.Identity.Security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Finance", description = "API Tài Chính")
public class FinanceController {
    private final PaymentService paymentService;
    private final WalletService walletService;
    private final BankAccountService bankAccountService;

    @GetMapping("/vnpay/booking/create-url")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "USER - Tạo URL thanh toán VNPAY cho booking")
    public ResponseEntity<ResponseData<String>> createVNPayUrl(
            @RequestParam String bookingCode,
            HttpServletRequest request) {

        String paymentUrl = paymentService.createBookingVNPayUrl(bookingCode, request);

        return ResponseEntity.ok(
                new ResponseData<>(200, "Tạo URL thanh toán thành công", paymentUrl)
        );
    }

    @GetMapping("/vnpay/subscription/create-url")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @Operation(summary = "HOST - Tạo URL thanh toán VNPAY để đăng ký/nâng cấp gói cước")
    public ResponseEntity<ResponseData<String>> createSubscriptionVNPayUrl(
            @RequestParam Long planId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {

        String paymentUrl = paymentService.createSubscriptionVNPayUrl(planId, userDetails.getUser().getId(), request);

        return ResponseEntity.ok(
                new ResponseData<>(200, "Tạo URL thanh toán VNPAY thành công", paymentUrl)
        );
    }

    @GetMapping("/vnpay/ipn")
    @Operation(summary = "(No testing required)VNPAY IPN Webhook")
    public ResponseEntity<Map<String, String>> processVNPayIPN(HttpServletRequest request) {
        Map<String, String> response = paymentService.processVnPayIpn(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "HOST - Xem thông tin ví")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/host/wallet")
    public ResponseEntity<ResponseData<WalletResponse>> getMyWallet(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        WalletResponse response = walletService.getMyWallet(userDetails.getUser().getId());
        return ResponseEntity.ok(new ResponseData<>(200, "Lấy thông tin ví thành công", response));
    }

    @Operation(summary = "HOST - Xem lịch sử giao dịch")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @GetMapping("/host/transactions")
    public ResponseEntity<ResponseData<PageResponse<TransactionResponse>>> getMyTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @Parameter(
                    name = "balanceAffected",
                    description = "(available, pending, debt)"
            )
            @RequestParam(required = false) String balanceAffected,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {

        PageResponse<TransactionResponse> response = walletService.getMyTransactions(
                userDetails.getUser().getId(), balanceAffected, pageNo, pageSize);

        return ResponseEntity.ok(new ResponseData<>(200, "Lấy lịch sử giao dịch thành công", response));
    }

    @Operation(summary = "HOST - Thêm tài khoản ngân hàng")
    @PostMapping("/host/banks")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    public ResponseEntity<ResponseData<Void>> addBank(
            @Valid @RequestBody BankAccountRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        bankAccountService.addBankAccount(user.getUser().getId(), request);
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.CREATED.value(), "Thêm tài khoản ngân hàng thành công", null));
    }

    @Operation(summary = "HOST - Xem danh sách tài khoản ngân hàng")
    @GetMapping("/host/banks")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    public ResponseEntity<ResponseData<List<BankAccountResponse>>> getMyBanks(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(new ResponseData<>(200, "Thành công", bankAccountService.getMyBankAccounts(user.getUser().getId())));
    }

    @Operation(summary = "HOST - Xóa tài khoản ngân hàng")
    @DeleteMapping("host/banks/{bankAccountId}")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    public ResponseEntity<ResponseData<Void>> deleteBank(
            @Parameter(
                    name = "bankAccountId",
                    description = "ID của tài khoản ngân hàng cần xóa - xem ở bảng bank_accounts"
            )
            @PathVariable Integer bankAccountId,
            @AuthenticationPrincipal CustomUserDetails user) {

        bankAccountService.deleteBankAccount(user.getUser().getId(), bankAccountId);

        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Xóa tài khoản ngân hàng thành công"));
    }

    @Operation(summary = "HOST - Valid yêu cầu rút tiền và tạo OTP")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PostMapping("host/payouts/request-otp")
    public ResponseEntity<ResponseData<String>> requestPayoutOtp(
            @Valid @RequestBody PayoutCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        walletService.requestPayoutOTP(userDetails.getUser().getId(), request);

        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Vui lòng kiểm tra email để lấy mã OTP xác nhận rút tiền"));
    }

    @Operation(summary = "HOST - Xác thực OTP và lưu yêu cầu rút tiền")
    @PreAuthorize("hasAuthority('ROLE_HOST')")
    @PostMapping("host/payouts/verify")
    public ResponseEntity<ResponseData<String>> verifyPayout(
            @Valid @RequestBody PayoutVerifyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        walletService.verifyAndCreatePayout(userDetails.getUser().getId(), request);

        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Xác thực OTP thành công. Yêu cầu rút tiền của bạn đã được gửi đến admin để thẩm định"));
    }

    @Operation(summary = "ADMIN - Lấy danh sách yêu cầu rút tiền")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("admin/payouts")
    public ResponseEntity<ResponseData<PageResponse<PayoutResponse>>> getAllPayouts(
            @Parameter(
                    name = "status",
                    description = "Lọc theo status Payout (REQUESTED, PROCESSING, COMPLETED, REJECTED)"
            )
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponse<PayoutResponse> response = walletService.getAllPayoutsForAdmin(status, pageNo, pageSize, sortBy, sortDir);

        return ResponseEntity.ok(new ResponseData<>(200, "Lấy danh sách thành công", response));
    }

    @Operation(summary = "ADMIN - Thẩm định lệnh rút tiền của Chủ nhà")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("admin/payouts/{payoutId}")
    public ResponseEntity<ResponseData<String>> processPayout(
            @Parameter(
                    name = "payoutId",
                    description = "ID của lệnh rút tiền - xem ở bảng payouts"
            )
            @PathVariable Long payoutId,
            @Valid @RequestBody PayoutProcessRequest request) {

        return ResponseEntity.ok(new ResponseData<>(200, walletService.processPayoutRequestByAdmin(payoutId, request)));
    }
}
