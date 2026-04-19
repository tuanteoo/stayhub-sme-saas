package com.stayhub.backend.Module.Finance.DTO.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PayoutProcessRequest(
        @Schema(description = "Có duyệt yêu cầu rút tiền hay không", example = "true")
        @NotNull(message = "Phải có quyết định duyệt hay không")
        Boolean isApproved,

        @Schema(description = "Mã giao dịch ngân hàng (nếu duyệt thì không được null)", example = "BANK123456789")
        String bankTransactionRef,

        @Schema(description = "Ảnh chụp biên lai chuyển khoản (nếu có)", example = "https://example.com/proof.jpg")
        String proofImageUrl,

        @Schema(description = "Ghi chú của admin khi xử lý yêu cầu rút tiền (nếu từ chối không được null)", example = "Yêu cầu đã được duyệt, sẽ chuyển khoản trong vòng 24h")
        String adminNote
) {
}
