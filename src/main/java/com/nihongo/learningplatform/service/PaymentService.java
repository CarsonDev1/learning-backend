package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.PaymentDto;
import com.nihongo.learningplatform.dto.VnPayRequestDto;
import com.nihongo.learningplatform.entity.Payment;
import com.nihongo.learningplatform.entity.PaymentStatus;

import java.util.List;
import java.util.Map;

public interface PaymentService {
    PaymentDto createPayment(PaymentDto paymentDto);
    PaymentDto getPaymentById(Long id);
    PaymentDto getPaymentByTransactionId(String transactionId);
    PaymentDto getPaymentByEnrollment(Long enrollmentId);
    Payment getPaymentEntityById(Long id);
    List<PaymentDto> getPaymentsByStatus(PaymentStatus status);
    PaymentDto updatePaymentStatus(Long id, PaymentStatus status);
    void deletePayment(Long id);

    // VNPay integration
    String createVnPayPaymentUrl(VnPayRequestDto requestDto);
    Map<String, String> processVnPayReturn(Map<String, String> queryParams);

    List<PaymentDto> getAllPayments();
}
