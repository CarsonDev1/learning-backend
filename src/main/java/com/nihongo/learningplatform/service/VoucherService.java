package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.VoucherDto;
import com.nihongo.learningplatform.dto.VoucherUsageDto;
import com.nihongo.learningplatform.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface VoucherService {
    VoucherDto createVoucher(VoucherDto voucherDto);
    VoucherDto getVoucherById(Long id);
    VoucherDto getVoucherByCode(String code);
    List<VoucherDto> getAllVouchers();
    List<VoucherDto> getActiveVouchers();
    List<VoucherDto> getValidVouchers();
    List<VoucherDto> getVouchersByCourse(Long courseId);
    List<VoucherDto> getValidVouchersByCourse(Long courseId);
    List<VoucherDto> getGlobalVouchers();
    VoucherDto updateVoucher(Long id, VoucherDto voucherDto);
    VoucherDto activateVoucher(Long id);
    VoucherDto deactivateVoucher(Long id);
    void deleteVoucher(Long id);

    // Validate and apply vouchers
    boolean isVoucherValid(String code, Long courseId);
    BigDecimal calculateDiscountAmount(String code, Long courseId, BigDecimal originalPrice);
    VoucherUsageDto applyVoucher(String code, Long courseId, Long userId, Long paymentId);

    // Voucher usage tracking
    List<VoucherUsageDto> getVoucherUsages();
    List<VoucherUsageDto> getVoucherUsagesByUser(Long userId);
    List<VoucherUsageDto> getVoucherUsagesByVoucher(Long voucherId);
    List<VoucherUsageDto> getVoucherUsagesByCourse(Long courseId);
    void deleteVoucherUsage(Long id);
}