package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.VoucherDto;
import com.nihongo.learningplatform.dto.VoucherUsageDto;
import com.nihongo.learningplatform.entity.*;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.exception.BadRequestException;
import com.nihongo.learningplatform.repository.VoucherRepository;
import com.nihongo.learningplatform.repository.VoucherUsageRepository;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.PaymentService;
import com.nihongo.learningplatform.service.UserService;
import com.nihongo.learningplatform.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final CourseService courseService;
    private final UserService userService;
    private final PaymentService paymentService;

    @Autowired
    public VoucherServiceImpl(VoucherRepository voucherRepository,
                              VoucherUsageRepository voucherUsageRepository,
                              CourseService courseService,
                              UserService userService,
                              PaymentService paymentService) {
        this.voucherRepository = voucherRepository;
        this.voucherUsageRepository = voucherUsageRepository;
        this.courseService = courseService;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional
    public VoucherDto createVoucher(VoucherDto voucherDto) {
        // Check if voucher code already exists
        if (voucherRepository.findByCode(voucherDto.getCode()).isPresent()) {
            throw new BadRequestException("Voucher code already exists");
        }

        Voucher voucher = new Voucher();
        voucher.setCode(voucherDto.getCode());
        voucher.setDescription(voucherDto.getDescription());
        voucher.setDiscountAmount(voucherDto.getDiscountAmount());
        voucher.setMinimumPurchaseAmount(voucherDto.getMinimumPurchaseAmount());
        voucher.setValidFrom(voucherDto.getValidFrom());
        voucher.setValidTo(voucherDto.getValidTo());
        voucher.setMaxUsage(voucherDto.getMaxUsage());
        voucher.setActive(voucherDto.isActive());

        // Set course if applicable
        if (voucherDto.getCourseId() != null) {
            Course course = courseService.getCourseEntityById(voucherDto.getCourseId());
            voucher.setCourse(course);
        }

        Voucher savedVoucher = voucherRepository.save(voucher);
        return mapToDto(savedVoucher);
    }

    @Override
    public VoucherDto getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));
        return mapToDto(voucher);
    }

    @Override
    public VoucherDto getVoucherByCode(String code) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + code));
        return mapToDto(voucher);
    }

    @Override
    public List<VoucherDto> getAllVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll();
        return vouchers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDto> getActiveVouchers() {
        List<Voucher> vouchers = voucherRepository.findByActive(true);
        return vouchers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDto> getValidVouchers() {
        List<Voucher> vouchers = voucherRepository.findAllValidVouchers(LocalDateTime.now());
        return vouchers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDto> getVouchersByCourse(Long courseId) {
        Course course = courseService.getCourseEntityById(courseId);
        List<Voucher> vouchers = voucherRepository.findByCourse(course);
        return vouchers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDto> getValidVouchersByCourse(Long courseId) {
        Course course = courseService.getCourseEntityById(courseId);
        List<Voucher> vouchers = voucherRepository.findValidVouchersForCourse(course, LocalDateTime.now());
        return vouchers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDto> getGlobalVouchers() {
        List<Voucher> vouchers = voucherRepository.findByCourseIsNull();
        return vouchers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VoucherDto updateVoucher(Long id, VoucherDto voucherDto) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));

        // Check if updated code already exists for other voucher
        if (!voucher.getCode().equals(voucherDto.getCode()) &&
                voucherRepository.findByCode(voucherDto.getCode()).isPresent()) {
            throw new BadRequestException("Voucher code already exists");
        }

        voucher.setCode(voucherDto.getCode());
        voucher.setDescription(voucherDto.getDescription());
        voucher.setDiscountAmount(voucherDto.getDiscountAmount());
        voucher.setMinimumPurchaseAmount(voucherDto.getMinimumPurchaseAmount());
        voucher.setValidFrom(voucherDto.getValidFrom());
        voucher.setValidTo(voucherDto.getValidTo());
        voucher.setMaxUsage(voucherDto.getMaxUsage());
        voucher.setActive(voucherDto.isActive());

        // Update course if applicable
        if (voucherDto.getCourseId() != null) {
            Course course = courseService.getCourseEntityById(voucherDto.getCourseId());
            voucher.setCourse(course);
        } else {
            voucher.setCourse(null);
        }

        Voucher updatedVoucher = voucherRepository.save(voucher);
        return mapToDto(updatedVoucher);
    }

    @Override
    @Transactional
    public VoucherDto activateVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));

        voucher.setActive(true);
        Voucher updatedVoucher = voucherRepository.save(voucher);
        return mapToDto(updatedVoucher);
    }

    @Override
    @Transactional
    public VoucherDto deactivateVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));

        voucher.setActive(false);
        Voucher updatedVoucher = voucherRepository.save(voucher);
        return mapToDto(updatedVoucher);
    }

    @Override
    @Transactional
    public void deleteVoucher(Long id) {
        if (!voucherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Voucher not found with id: " + id);
        }
        voucherRepository.deleteById(id);
    }

    @Override
    public boolean isVoucherValid(String code, Long courseId) {
        // Find voucher by code
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + code));

        // Check if voucher is valid
        if (!voucher.isValid()) {
            return false;
        }

        // Check if voucher is applicable to this course
        Course course = courseService.getCourseEntityById(courseId);
        return voucher.getCourse() == null || voucher.getCourse().getId().equals(courseId);
    }

    @Override
    public BigDecimal calculateDiscountAmount(String code, Long courseId, BigDecimal originalPrice) {
        // Find voucher by code
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + code));

        // Check if voucher is valid
        if (!voucher.isValid()) {
            throw new BadRequestException("Voucher is not valid");
        }

        // Check if voucher is applicable to this course
        Course course = courseService.getCourseEntityById(courseId);
        if (voucher.getCourse() != null && !voucher.getCourse().getId().equals(courseId)) {
            throw new BadRequestException("Voucher is not applicable to this course");
        }

        // Check minimum purchase amount
        if (originalPrice.compareTo(voucher.getMinimumPurchaseAmount()) < 0) {
            throw new BadRequestException("Order does not meet minimum purchase amount for this voucher");
        }

        // Calculate discount amount
        BigDecimal discountAmount = voucher.getDiscountAmount();

        // Ensure discount doesn't exceed original price
        return discountAmount.min(originalPrice);
    }

    @Override
    @Transactional
    public VoucherUsageDto applyVoucher(String code, Long courseId, Long userId, Long paymentId) {
        // Validate voucher
        if (!isVoucherValid(code, courseId)) {
            throw new BadRequestException("Voucher is not valid");
        }

        // Get entities
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + code));
        User user = userService.getUserEntityById(userId);
        Course course = courseService.getCourseEntityById(courseId);
        Payment payment = null;
        if (paymentId != null) {
            payment = paymentService.getPaymentEntityById(paymentId);
        }

        // Check if voucher has already been used by this user
        if (voucherUsageRepository.existsByUserAndVoucher(user, voucher)) {
            throw new BadRequestException("Voucher has already been used by this user");
        }

        // Create voucher usage
        VoucherUsage voucherUsage = new VoucherUsage();
        voucherUsage.setVoucher(voucher);
        voucherUsage.setUser(user);
        voucherUsage.setCourse(course);
        voucherUsage.setPayment(payment);

        VoucherUsage savedVoucherUsage = voucherUsageRepository.save(voucherUsage);

        // Increment voucher usage count
        voucher.setUsageCount(voucher.getUsageCount() + 1);
        voucherRepository.save(voucher);

        return mapToUsageDto(savedVoucherUsage);
    }

    @Override
    public List<VoucherUsageDto> getVoucherUsages() {
        List<VoucherUsage> voucherUsages = voucherUsageRepository.findAll();
        return voucherUsages.stream()
                .map(this::mapToUsageDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherUsageDto> getVoucherUsagesByUser(Long userId) {
        User user = userService.getUserEntityById(userId);
        List<VoucherUsage> voucherUsages = voucherUsageRepository.findByUser(user);
        return voucherUsages.stream()
                .map(this::mapToUsageDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherUsageDto> getVoucherUsagesByVoucher(Long voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + voucherId));
        List<VoucherUsage> voucherUsages = voucherUsageRepository.findByVoucher(voucher);
        return voucherUsages.stream()
                .map(this::mapToUsageDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherUsageDto> getVoucherUsagesByCourse(Long courseId) {
        Course course = courseService.getCourseEntityById(courseId);
        List<VoucherUsage> voucherUsages = voucherUsageRepository.findByCourse(course);
        return voucherUsages.stream()
                .map(this::mapToUsageDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteVoucherUsage(Long id) {
        if (!voucherUsageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Voucher usage not found with id: " + id);
        }
        voucherUsageRepository.deleteById(id);
    }

    // Helper methods to map entities to DTOs
    private VoucherDto mapToDto(Voucher voucher) {
        VoucherDto voucherDto = new VoucherDto();
        voucherDto.setId(voucher.getId());
        voucherDto.setCode(voucher.getCode());
        voucherDto.setDescription(voucher.getDescription());
        voucherDto.setDiscountAmount(voucher.getDiscountAmount());
        voucherDto.setMinimumPurchaseAmount(voucher.getMinimumPurchaseAmount());
        voucherDto.setValidFrom(voucher.getValidFrom());
        voucherDto.setValidTo(voucher.getValidTo());
        voucherDto.setMaxUsage(voucher.getMaxUsage());
        voucherDto.setUsageCount(voucher.getUsageCount());
        voucherDto.setActive(voucher.isActive());
        voucherDto.setCreatedAt(voucher.getCreatedAt());
        voucherDto.setUpdatedAt(voucher.getUpdatedAt());
        voucherDto.setValid(voucher.isValid());

        if (voucher.getCourse() != null) {
            voucherDto.setCourseId(voucher.getCourse().getId());
            voucherDto.setCourseName(voucher.getCourse().getTitle());
        }

        return voucherDto;
    }

    private VoucherUsageDto mapToUsageDto(VoucherUsage voucherUsage) {
        VoucherUsageDto voucherUsageDto = new VoucherUsageDto();
        voucherUsageDto.setId(voucherUsage.getId());
        voucherUsageDto.setVoucherId(voucherUsage.getVoucher().getId());
        voucherUsageDto.setVoucherCode(voucherUsage.getVoucher().getCode());
        voucherUsageDto.setUserId(voucherUsage.getUser().getId());
        voucherUsageDto.setUsername(voucherUsage.getUser().getUsername());
        voucherUsageDto.setCourseId(voucherUsage.getCourse().getId());
        voucherUsageDto.setCourseName(voucherUsage.getCourse().getTitle());

        if (voucherUsage.getPayment() != null) {
            voucherUsageDto.setPaymentId(voucherUsage.getPayment().getId());
        }

        voucherUsageDto.setUsedAt(voucherUsage.getUsedAt());

        return voucherUsageDto;
    }
}