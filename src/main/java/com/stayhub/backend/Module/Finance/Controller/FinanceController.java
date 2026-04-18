package com.stayhub.backend.Module.Finance.Controller;

import com.stayhub.backend.Common.DTO.Response.PageResponse;
import com.stayhub.backend.Common.DTO.Response.ResponseData;
import com.stayhub.backend.Module.Finance.DTO.Response.TransactionResponse;
import com.stayhub.backend.Module.Finance.DTO.Response.WalletResponse;
import com.stayhub.backend.Module.Finance.Service.PaymentService;
import com.stayhub.backend.Module.Finance.Service.WalletService;
import com.stayhub.backend.Module.Identity.Security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Finance", description = "API Tài Chính")
public class FinanceController {
    private final PaymentService paymentService;
    private final WalletService walletService;

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
}
