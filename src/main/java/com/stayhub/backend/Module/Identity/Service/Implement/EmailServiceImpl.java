package com.stayhub.backend.Module.Identity.Service.Implement;

import com.stayhub.backend.Module.Identity.Service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    //private String frontendUrl = "";

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

            String verifyUrl =  "http://localhost:8080/api/v1/auth/verify-email?token=" + token;
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
}
