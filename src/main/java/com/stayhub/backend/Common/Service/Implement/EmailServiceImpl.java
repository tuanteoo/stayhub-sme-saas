package com.stayhub.backend.Common.Service.Implement;

import com.stayhub.backend.Common.Exception.AppException;
import com.stayhub.backend.Common.Service.EmailService;
import com.stayhub.backend.Common.Util.BookingPaymentOption;
import com.stayhub.backend.Common.Util.ErrorCode;
import com.stayhub.backend.Module.Booking.Model.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${application.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Async("emailTaskExecutor")
    @Override
    public void sendVerificationEmailAsync(String toEmail, String fullName, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context();
            context.setVariable("name", fullName);

            String verifyUrl = frontendUrl + "/verify-email?token=" + token;
            context.setVariable("url", verifyUrl);

            String htmlContent = templateEngine.process("email/verify-email", context);

            helper.setFrom(fromEmail, "StayHub Support");
            helper.setTo(toEmail);
            helper.setSubject("Xác thực tài khoản StayHub của bạn");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email xác thực bất đồng bộ thành công tới: {}", toEmail);

        } catch (Exception e) {
            log.error("Lỗi khi gửi email xác thực tới {}: {}", toEmail, e.getMessage());
        }
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendBookingReceiptEmail(String toEmail, String guestName, Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            ClassPathResource resource = new ClassPathResource("templates/email/booking-receipt.html");
            String htmlContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

            BigDecimal totalAmount = booking.getTotalPrice().add(booking.getCleaningFee());
            String formatTotalAmount = currencyFormat.format(totalAmount);

            BigDecimal amountPaid = booking.getDepositAmount() != null ? booking.getDepositAmount() : BigDecimal.ZERO;
            String formattedAmountPaid = currencyFormat.format(amountPaid);

            BigDecimal remainingAmount = totalAmount.subtract(amountPaid);
            String formattedRemainingAmount = currencyFormat.format(remainingAmount);

            String paymentStatusStr;
            if (booking.getPaymentOption() == BookingPaymentOption.PAY_IN_FULL) {
                paymentStatusStr = "Đã thanh toán toàn bộ";
            } else {
                paymentStatusStr = "Đã thanh toán cọc";
            }

            // 3. Thay thế các biến trong HTML bằng data thật
            htmlContent = htmlContent.replace("{{guestName}}", guestName)
                    .replace("{{bookingCode}}", booking.getBookingCode())
                    .replace("{{propertyName}}", booking.getProperty().getName())
                    .replace("{{checkInDate}}", booking.getCheckInDate().toString())
                    .replace("{{checkOutDate}}", booking.getCheckOutDate().toString())
                    .replace("{{totalNights}}", String.valueOf(booking.getTotalNights()))
                    .replace("{{totalGuests}}", String.valueOf(booking.getTotalGuests()))
                    .replace("{{totalPrice}}", formatTotalAmount)
                    .replace("{{amountPaid}}", formattedAmountPaid)
                    .replace("{{remainingAmount}}", formattedRemainingAmount)
                    .replace("{{paymentStatus}}", paymentStatusStr);

            helper.setFrom(fromEmail, "StayHub Support");
            helper.setTo(toEmail);
            helper.setSubject("🏡 Biên lai đặt phòng của bạn tại StayHub - " + booking.getBookingCode());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email biên lai thành công cho booking: {}", booking.getBookingCode());

        } catch (Exception e) {
            log.error("Lỗi khi gửi email biên lai cho booking {}: {}", booking.getBookingCode(), e.getMessage());
        }
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendHostApprovalEmail(String toEmail, String hostName) {
        try {
            Context context = new Context();
            context.setVariable("hostName", hostName);

            String htmlContent = templateEngine.process("email/host-approval", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🎉 Chúc mừng! Đơn đăng ký Chủ nhà StayHub của bạn đã được phê duyệt");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendPasswordResetEmail(String toEmail, String guestName, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            Context context = new Context();
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("fullName", guestName);

            String htmlContent = templateEngine.process("email/forgot-password", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "StayHub Support");
            helper.setTo(toEmail);
            helper.setSubject("Yêu cầu đặt lại mật khẩu - StayHub");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email đặt lại mật khẩu thành công đến: {}", toEmail);

        } catch (Exception e) {
            log.error("Lỗi khi gửi email đặt lại mật khẩu đến {}: {}", toEmail, e.getMessage());
        }
    }
}
