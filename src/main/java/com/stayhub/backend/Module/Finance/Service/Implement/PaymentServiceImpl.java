package com.stayhub.backend.Module.Finance.Service.Implement;

import com.stayhub.backend.Common.Exception.InvalidDataException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Service.EmailService;
import com.stayhub.backend.Module.Booking.Service.BookingService;
import com.stayhub.backend.Module.Finance.Model.Payment;
import com.stayhub.backend.Module.Finance.Repository.PaymentRepository;
import com.stayhub.backend.Module.Finance.Service.PaymentService;
import com.stayhub.backend.Common.Util.BookingPaymentOption;
import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Common.Util.PaymentStatus;
import com.stayhub.backend.Config.VNPayConfig;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Finance.Service.WalletService;
import com.stayhub.backend.Module.Identity.Model.User;
import com.stayhub.backend.Module.Property.Model.SubscriptionPlan;
import com.stayhub.backend.Module.Property.Repository.SubscriptionPlanRepository;
import com.stayhub.backend.Module.Property.Service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionService subscriptionService;
    private final WalletService walletService;

    @Lazy
    @Autowired
    private BookingService bookingService;

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.pay-url}")
    private String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.version}")
    private String vnp_Version;

    @Value("${vnpay.command}")
    private String vnp_Command;

    @Value("${vnpay.api-url}")
    private String vnp_ApiUrl;

    @Override
    public Map<String, String> processVnPayIpn(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            List<String> fieldNames = new ArrayList<>(fields.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);
                if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8).replace("+", "%20"));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            String signValue = VNPayConfig.hmacSHA512(vnp_HashSecret.trim(), hashData.toString());
            if (!signValue.equals(vnp_SecureHash)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid Checksum");
                return response;
            }

            String vnp_TxnRef = request.getParameter("vnp_TxnRef");

            if (vnp_TxnRef.startsWith("SHB-")) {
                return handleBookingPayment(vnp_TxnRef, fields);
            }
            else if (vnp_TxnRef.startsWith("SUB-")) {
                long vnpAmount = Long.parseLong(request.getParameter("vnp_Amount"));
                String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
                return handleSubscriptionPayment(vnp_TxnRef, vnpAmount, vnp_ResponseCode);
            }
            else {
                response.put("RspCode", "01");
                response.put("Message", "Order not found (Unknown Prefix)");
                return response;
            }

        } catch (Exception e) {
            log.error("Lỗi IPN: {}", e.getMessage());
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
            return response;
        }
    }

    @Override
    public String createBookingVNPayUrl(String bookingCode, HttpServletRequest request) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt phòng"));

        if (booking.getStatus() != BookingStatus.AWAITING_PAYMENT) {
            throw new InvalidDataException("Đơn đặt phòng này không ở trạng thái chờ thanh toán!");
        }

        Payment pendingPayment = paymentRepository.findFirstByBooking_IdAndPaymentStatusOrderByCreatedAtDesc(
                        booking.getId(), PaymentStatus.PENDING)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy hồ sơ thanh toán gốc của đơn hàng này."));

        long amount = pendingPayment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        String vnp_TxnRef = pendingPayment.getTransactionRef();
        String vnp_OrderInfo = "Thanh_toan_don_hang_" + bookingCode;

        return buildVNPayUrl(amount, vnp_TxnRef, vnp_OrderInfo, request);
    }

    @Override
    public String createSubscriptionVNPayUrl(Long planId, Long hostId, HttpServletRequest request) {
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Gói cước không tồn tại"));

        if (subscriptionPlan.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDataException("Gói này miễn phí, không cần thanh toán qua VNPAY!");
        }

        long amount = subscriptionPlan.getPrice().multiply(BigDecimal.valueOf(100)).longValue();
        String vnp_TxnRef = "SUB-" + hostId + "-" + planId + "_" + VNPayConfig.getRandomNumber(6);
        String vnp_OrderInfo = "Thanh_toan_goi_cuoc_" + subscriptionPlan.getTier().toString();

        return buildVNPayUrl(amount, vnp_TxnRef, vnp_OrderInfo, request);
    }

    @Override
    public boolean refundVnPayTransaction(Payment originalPayment, BigDecimal refundAmount) {
        log.info("Khởi tạo yêu cầu hoàn tiền sang VNPAY cho GD: {}", originalPayment.getGatewayTransactionNo());

        try {
            String vnp_RequestId = VNPayConfig.getRandomNumber(8);
            String vnp_Version = "2.1.0";
            String vnp_Command = "refund";
            String vnp_TmnCode = this.vnp_TmnCode;

            // 02: Hoàn tiền toàn phần, 03: Hoàn tiền một phần
            String vnp_TransactionType = refundAmount.compareTo(originalPayment.getAmount()) == 0 ? "02" : "03";

            String vnp_TxnRef = originalPayment.getTransactionRef();
            long amount = refundAmount.multiply(new BigDecimal(100)).longValue();
            String vnp_Amount = String.valueOf(amount);
            String vnp_OrderInfo = "Hoan tien don hang " + vnp_TxnRef;

            String vnp_TransactionNo = originalPayment.getGatewayTransactionNo() != null ? originalPayment.getGatewayTransactionNo() : "";
            String vnp_TransactionDate = originalPayment.getPayDate();
            String vnp_CreateBy = "SYSTEM";

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());

            String vnp_IpAddr = "127.0.0.1";

            // Quy tắc tạo checksum: nối các tham số bằng dấu |
            String hashData = vnp_RequestId + "|" + vnp_Version + "|" + vnp_Command + "|" + vnp_TmnCode + "|" +
                    vnp_TransactionType + "|" + vnp_TxnRef + "|" + vnp_Amount + "|" + vnp_TransactionNo + "|" +
                    vnp_TransactionDate + "|" + vnp_CreateBy + "|" + vnp_CreateDate + "|" + vnp_IpAddr + "|" + vnp_OrderInfo;

            String vnp_SecureHash = VNPayConfig.hmacSHA512(vnp_HashSecret, hashData);

            Map<String, Object> payload = new HashMap<>();
            payload.put("vnp_RequestId", vnp_RequestId);
            payload.put("vnp_Version", vnp_Version);
            payload.put("vnp_Command", vnp_Command);
            payload.put("vnp_TmnCode", vnp_TmnCode);
            payload.put("vnp_TransactionType", vnp_TransactionType);
            payload.put("vnp_TxnRef", vnp_TxnRef);
            payload.put("vnp_Amount", vnp_Amount);
            payload.put("vnp_TransactionNo", vnp_TransactionNo);
            payload.put("vnp_TransactionDate", vnp_TransactionDate);
            payload.put("vnp_CreateBy", vnp_CreateBy);
            payload.put("vnp_CreateDate", vnp_CreateDate);
            payload.put("vnp_IpAddr", vnp_IpAddr);
            payload.put("vnp_OrderInfo", vnp_OrderInfo);
            payload.put("vnp_SecureHash", vnp_SecureHash);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(vnp_ApiUrl, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && "00".equals(responseBody.get("vnp_ResponseCode"))) {
                log.info("VNPAY báo hoàn tiền THÀNH CÔNG cho đơn: {}", originalPayment.getBooking().getBookingCode());
                return true;
            } else {
                log.error("VNPAY báo hoàn tiền THẤT BẠI: {}", responseBody != null ? responseBody.get("vnp_Message") : "Không có phản hồi");
                return false;
            }
        } catch (Exception e) {
            log.error("Lỗi khi thực hiện hoàn tiền VNPAY: {}", e.getMessage());
            return false;
        }
    }

    private Map<String, String> handleBookingPayment(String vnp_TxnRef, Map<String, String> fields) {
        Map<String, String> response = new HashMap<>();
        long vnpAmount = Long.parseLong(fields.get("vnp_Amount"));
        String vnp_ResponseCode = fields.get("vnp_ResponseCode");
        String vnp_TransactionNo = fields.get("vnp_TransactionNo");
        String vnp_PayDate = fields.get("vnp_PayDate");

        Payment payment = paymentRepository.findByTransactionRef(vnp_TxnRef)
                .orElseThrow(() -> new ResourceNotFoundException("Giao dịch không tồn tại"));

        Booking booking = payment.getBooking();

        long expectedAmount = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        if (vnpAmount != expectedAmount) {
            response.put("RspCode", "04");
            response.put("Message", "Invalid amount");
            return response;
        }

        payment.setGatewayTransactionNo(vnp_TransactionNo);
        payment.setPayDate(vnp_PayDate);
        payment.setGatewayResponseCode(vnp_ResponseCode);
        payment.setGatewayPayload(fields.toString());

        if ("00".equals(vnp_ResponseCode)) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            emailService.sendBookingReceiptEmail(booking.getUser().getEmail(), booking.getUser().getProfile().getFullName(), booking);
            walletService.processBookingPaymentSuccess(booking, payment.getAmount());
            if (booking.getPaymentOption() == BookingPaymentOption.PAY_IN_FULL) {
                booking.setStatus(BookingStatus.CONFIRMED);
            } else {
                booking.setStatus(BookingStatus.PARTIALLY_PAID);
            }
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            bookingService.releaseBookingInternal(booking, BookingStatus.CANCELLED, booking.getUser().getId());
        }

        paymentRepository.save(payment);
        bookingRepository.save(booking);

        response.put("RspCode", "00");
        response.put("Message", "Confirm Success");
        return response;
    }

    private Map<String, String> handleSubscriptionPayment(String orderCode, long vnpAmount, String vnp_ResponseCode) {
        Map<String, String> response = new HashMap<>();
        try {
            String[] parts = orderCode.split("-");
            Long hostId = Long.parseLong(parts[1]);
            Long planId = Long.parseLong(parts[2].split("_")[0]);

            SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                    .orElseThrow(() -> new ResourceNotFoundException("Gói cước không tồn tại"));

            long expectedAmount = plan.getPrice().multiply(BigDecimal.valueOf(100)).longValue();
            if (vnpAmount != expectedAmount) {
                response.put("RspCode", "04");
                response.put("Message", "Invalid amount");
                return response;
            }

            if ("00".equals(vnp_ResponseCode)) {
                subscriptionService.processSubscriptionPurchase(hostId, planId);
            }
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return response;
        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Lỗi xử lý gói cước");
            return response;
        }
    }

    private String buildVNPayUrl(long amount, String vnp_TxnRef, String vnp_OrderInfo, HttpServletRequest request) {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", VNPayConfig.getIpAddress(request));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return vnp_PayUrl + "?" + queryUrl;
    }
}