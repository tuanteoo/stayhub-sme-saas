package com.stayhub.backend.Common.Service;

import com.stayhub.backend.Module.Booking.Model.Booking;

public interface EmailService {
    void sendVerificationEmailAsync(String toEmail, String fullName, String token);
    void sendBookingReceiptEmail(String toEmail, String guestName, Booking booking);
    void sendHostApprovalEmail(String toEmail, String hostName);
    void sendPasswordResetEmail(String toEmail, String token);
}
