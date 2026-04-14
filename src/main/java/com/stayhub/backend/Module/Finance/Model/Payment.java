package com.stayhub.backend.Module.Finance.Model;

import com.stayhub.backend.Common.Model.AbstractEntity;
import com.stayhub.backend.Common.Util.PaymentMethod;
import com.stayhub.backend.Common.Util.PaymentPurpose;
import com.stayhub.backend.Common.Util.PaymentStatus;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Identity.Model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private PaymentPurpose paymentPurpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @Column(name = "gateway_transaction_no", length = 255)
    private String gatewayTransactionNo;

    @Column(name = "bank_code", length = 50)
    private String bankCode;

    @Column(name = "pay_date", length = 50)
    private String payDate;

    @Column(name = "gateway_response_code", length = 50)
    private String gatewayResponseCode;

    @Column(name = "gateway_payload", columnDefinition = "TEXT")
    private String gatewayPayload;

    @Column(name = "refund_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal refundAmount = BigDecimal.ZERO;
}
