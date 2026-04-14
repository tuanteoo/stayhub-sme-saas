package com.stayhub.backend.Module.Finance.Repository;

import com.stayhub.backend.Common.Util.PaymentStatus;
import com.stayhub.backend.Module.Finance.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByTransactionRef(String transactionRef);
    Optional<Payment> findFirstByBooking_IdAndPaymentStatusOrderByCreatedAtDesc(Long bookingId, PaymentStatus paymentStatus);
}
