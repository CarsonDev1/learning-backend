package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.EnrollmentDto;
import com.nihongo.learningplatform.dto.PaymentDto;
import com.nihongo.learningplatform.dto.VnPayRequestDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Enrollment;
import com.nihongo.learningplatform.entity.Payment;
import com.nihongo.learningplatform.entity.PaymentStatus;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.PaymentRepository;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.EnrollmentService;
import com.nihongo.learningplatform.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final EnrollmentService enrollmentService;
    private final CourseService courseService;

    @Value("${vnpay.vnp_TmnCode}")
    private String vnpTmnCode;

    @Value("${vnpay.vnp_HashSecret}")
    private String vnpHashSecret;

    @Value("${vnpay.vnp_PayUrl}")
    private String vnpPayUrl;

    @Value("${vnpay.vnp_ReturnUrl}")
    private String vnpReturnUrl;

    @Value("${vnpay.vnp_ApiUrl}")
    private String vnpApiUrl;

    @Value("${vnpay.vnp_Version}")
    private String vnpVersion;

    @Value("${vnpay.vnp_Command}")
    private String vnpCommand;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              EnrollmentService enrollmentService,
                              CourseService courseService) {
        this.paymentRepository = paymentRepository;
        this.enrollmentService = enrollmentService;
        this.courseService = courseService;
    }

    @Override
    @Transactional
    public PaymentDto createPayment(PaymentDto paymentDto) {
        Enrollment enrollment = enrollmentService.getEnrollmentEntityById(paymentDto.getEnrollmentId());

        Payment payment = new Payment();
        payment.setTransactionId(paymentDto.getTransactionId());
        payment.setAmount(paymentDto.getAmount());
        payment.setStatus(paymentDto.getStatus());
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setEnrollment(enrollment);

        Payment savedPayment = paymentRepository.save(payment);
        return mapToDto(savedPayment);
    }

    @Override
    public Payment getPaymentEntityById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    @Override
    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return mapToDto(payment);
    }

    @Override
    public PaymentDto getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction id: " + transactionId));
        return mapToDto(payment);
    }

    @Override
    public PaymentDto getPaymentByEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentService.getEnrollmentEntityById(enrollmentId);

        Payment payment = paymentRepository.findByEnrollment(enrollment)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for enrollment id: " + enrollmentId));

        return mapToDto(payment);
    }

    @Override
    public List<PaymentDto> getPaymentsByStatus(PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByStatus(status);
        return payments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentDto updatePaymentStatus(Long id, PaymentStatus status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        payment.setStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);
        return mapToDto(updatedPayment);
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }

    @Override
    public String createVnPayPaymentUrl(VnPayRequestDto requestDto) {
        Course course = courseService.getCourseEntityById(requestDto.getCourseId());

        // Create VNPay payment URL
        try {
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", vnpVersion);
            vnpParams.put("vnp_Command", vnpCommand);
            vnpParams.put("vnp_TmnCode", vnpTmnCode);
            vnpParams.put("vnp_Amount", String.valueOf(requestDto.getAmount().multiply(java.math.BigDecimal.valueOf(100)).intValue()));

            // Generate transaction ID
            String transactionId = String.valueOf(System.currentTimeMillis());
            vnpParams.put("vnp_TxnRef", transactionId);

            vnpParams.put("vnp_OrderInfo", requestDto.getOrderInfo() + " - " + course.getTitle());
            vnpParams.put("vnp_OrderType", "billpayment");

            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_IpAddr", "127.0.0.1");

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String vnpCreateDate = now.format(formatter);
            vnpParams.put("vnp_CreateDate", vnpCreateDate);

            // Calculate expiry date (15 minutes)
            LocalDateTime expireDate = now.plusMinutes(15);
            String vnpExpireDate = expireDate.format(formatter);
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            // Set return URL
            String returnUrl = requestDto.getReturnUrl() != null ? requestDto.getReturnUrl() : vnpReturnUrl;
            returnUrl += "?courseId=" + requestDto.getCourseId();
            vnpParams.put("vnp_ReturnUrl", returnUrl);

            // Sort parameters
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    // Build hash data
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                        hashData.append('&');
                        query.append('&');
                    }
                }
            }

            // Create secure hash
            String secureHash = hmacSHA512(vnpHashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(secureHash);

            return vnpPayUrl + "?" + query.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error creating VNPay payment URL", e);
        }
    }

    @Override
    @Transactional
    public Map<String, String> processVnPayReturn(Map<String, String> queryParams) {
        Map<String, String> result = new HashMap<>();

        try {
            // Get data from queryParams
            String vnpSecureHash = queryParams.get("vnp_SecureHash");
            String courseIdStr = queryParams.get("courseId");
            Long courseId = Long.parseLong(courseIdStr);

            // Remove vnp_SecureHash and vnp_SecureHashType
            queryParams.remove("vnp_SecureHash");
            queryParams.remove("vnp_SecureHashType");
            queryParams.remove("courseId");

            // Sort parameters
            List<String> fieldNames = new ArrayList<>(queryParams.keySet());
            Collections.sort(fieldNames);

            // Build hash data
            StringBuilder hashData = new StringBuilder();
            for (String fieldName : fieldNames) {
                String fieldValue = queryParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                        hashData.append('&');
                    }
                }
            }

            // Verify secure hash
            String secureHash = hmacSHA512(vnpHashSecret, hashData.toString());

            if (secureHash.equals(vnpSecureHash)) {
                // Hash is valid, check payment status
                String responseCode = queryParams.get("vnp_ResponseCode");

                if ("00".equals(responseCode)) {
                    // Payment successful
                    String transactionId = queryParams.get("vnp_TxnRef");
                    String amount = queryParams.get("vnp_Amount");
                    String studentId = ""; // Get student ID from security context

                    // Create enrollment
                    EnrollmentDto enrollmentDto = enrollmentService.createEnrollmentAfterPayment(courseId, Long.parseLong(studentId));

                    // Create payment record
                    PaymentDto paymentDto = new PaymentDto();
                    paymentDto.setTransactionId(transactionId);
                    paymentDto.setAmount(java.math.BigDecimal.valueOf(Long.parseLong(amount)).divide(java.math.BigDecimal.valueOf(100)));
                    paymentDto.setStatus(PaymentStatus.COMPLETED);
                    paymentDto.setPaymentMethod("VNPAY");
                    paymentDto.setEnrollmentId(enrollmentDto.getId());

                    createPayment(paymentDto);

                    result.put("status", "success");
                    result.put("message", "Payment successful");
                } else {
                    // Payment failed
                    result.put("status", "error");
                    result.put("message", "Payment failed with response code: " + responseCode);
                }
            } else {
                // Invalid hash
                result.put("status", "error");
                result.put("message", "Invalid security hash");
            }

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Error processing payment return: " + e.getMessage());
        }

        return result;
    }

    // Helper method for HMAC_SHA512 encryption
    private String hmacSHA512(String key, String data) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            sha512_HMAC.init(secretKey);

            byte[] hash = sha512_HMAC.doFinal(data.getBytes());
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error creating HMAC SHA512", e);
        }
    }

    // Helper method to convert bytes to hex
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // Helper method to map Payment entity to PaymentDto
    private PaymentDto mapToDto(Payment payment) {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(payment.getId());
        paymentDto.setTransactionId(payment.getTransactionId());
        paymentDto.setAmount(payment.getAmount());
        paymentDto.setStatus(payment.getStatus());
        paymentDto.setPaymentMethod(payment.getPaymentMethod());
        paymentDto.setPaymentDate(payment.getPaymentDate());
        paymentDto.setEnrollmentId(payment.getEnrollment().getId());
        return paymentDto;
    }

    @Override
    public List<PaymentDto> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}