package com.stayhub.backend.Module.Booking.Service.Implement;

import com.stayhub.backend.Common.Exception.InvalidDataException;
import com.stayhub.backend.Common.Exception.ResourceNotFoundException;
import com.stayhub.backend.Common.Util.BookingPaymentOption;
import com.stayhub.backend.Common.Util.BookingStatus;
import com.stayhub.backend.Config.VNPayConfig;
import com.stayhub.backend.Module.Booking.Model.Booking;
import com.stayhub.backend.Module.Booking.Model.BookingRoom;
import com.stayhub.backend.Module.Booking.Repository.BookingRepository;
import com.stayhub.backend.Module.Booking.Service.PaymentService;
import com.stayhub.backend.Module.Property.Repository.RoomAvailabilityRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final BookingRepository bookingRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;

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


    @Override
    public String createVNPayUrl(String bookingCode, HttpServletRequest request) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt phòng"));

        // Kiểm tra trạng thái đơn hàng
        if (booking.getStatus() != BookingStatus.AWAITING_PAYMENT) {
            throw new InvalidDataException("Đơn đặt phòng này không ở trạng thái chờ thanh toán!");
        }

        // Kiểm tra tiền cọc
        BigDecimal amountToPay = booking.getDepositAmount();
        if (amountToPay == null || amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDataException("Đơn này có số tiền cọc = 0đ, không cần thanh toán qua VNPAY!");
        }

        long amount = amountToPay.multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        // Mã giao dịch ghép từ BookingCode và Ramdom để tránh trùng lặp nếu thanh toán lại
        String vnp_TxnRef = bookingCode + "_" + VNPayConfig.getRandomNumber(6);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh_toan_don_hang_" + bookingCode); // Không dùng dấu cách
        vnp_Params.put("vnp_OrderType", "170000"); // Mã ngành Khách sạn/Du lịch chuẩn của VNPAY
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);

        // Chặn lỗi địa chỉ IPv6 localhost (::1) gây lỗi VNPAY
        String ipAddr = VNPayConfig.getIpAddress(request);
        if (ipAddr == null || ipAddr.isEmpty() || ipAddr.contains(":")) {
            ipAddr = "127.0.0.1";
        }
        vnp_Params.put("vnp_IpAddr", ipAddr);

        // Cấu hình múi giờ chuẩn xác Châu Á/HCM
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        // Thời hạn thanh toán (15 phút)
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sắp xếp danh sách tham số để mã hóa
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                    String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8).replace("+", "%20");

                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(encodedValue);

                    query.append(fieldName);
                    query.append('=');
                    query.append(encodedValue);

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo URL VNPAY", e);
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(vnp_HashSecret.trim(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnp_PayUrl + "?" + queryUrl;
    }

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

            // 3. IPN ROUTER: ĐIỀU HƯỚNG GIAO DỊCH
            String vnp_TxnRef = request.getParameter("vnp_TxnRef");
            String orderCode = vnp_TxnRef.split("_")[0];
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            long vnpAmount = Long.parseLong(request.getParameter("vnp_Amount"));

            if (orderCode.startsWith("SHB-")) {
                return handleBookingPayment(orderCode, vnpAmount, vnp_ResponseCode);
            }
            // else if (orderCode.startsWith("WAL-")) { return handleWalletTopup(...); }
            // else if (orderCode.startsWith("PKG-")) { return handlePremiumPackage(...); }
            else {
                response.put("RspCode", "01");
                response.put("Message", "Order not found (Unknown Prefix)");
                return response;
            }

        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
            return response;
        }
    }

    private Map<String, String> handleBookingPayment(String bookingCode, long vnpAmount, String vnp_ResponseCode) {
        Map<String, String> response = new HashMap<>();

        Optional<Booking> optionalBooking = bookingRepository.findByBookingCode(bookingCode);
        if (optionalBooking.isEmpty()) {
            response.put("RspCode", "01");
            response.put("Message", "Order not found");
            return response;
        }

        Booking booking = optionalBooking.get();

        if (booking.getStatus() != BookingStatus.AWAITING_PAYMENT) {
            response.put("RspCode", "02");
            response.put("Message", "Order already confirmed or processed");
            return response;
        }

        long expectedAmount = booking.getDepositAmount().multiply(BigDecimal.valueOf(100)).longValue();
        if (vnpAmount != expectedAmount) {
            response.put("RspCode", "04");
            response.put("Message", "Invalid amount");
            return response;
        }

        if ("00".equals(vnp_ResponseCode)) {
            if (booking.getPaymentOption() == BookingPaymentOption.PAY_IN_FULL) {
                booking.setStatus(BookingStatus.CONFIRMED);
            } else {
                booking.setStatus(BookingStatus.PARTIALLY_PAID);
            }
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancellationReason("Thanh toán VNPAY thất bại hoặc khách hàng hủy giao dịch");
            roomAvailabilityRepository.releaseRoomsByBooking(booking);
        }

        bookingRepository.save(booking);

        response.put("RspCode", "00");
        response.put("Message", "Confirm Success");
        return response;
    }
}
