package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.*;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.UserService;
import com.nihongo.learningplatform.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VoucherController {

    private final VoucherService voucherService;
    private final UserService userService;
    private final CourseService courseService;

    @Autowired
    public VoucherController(VoucherService voucherService,
                             UserService userService,
                             CourseService courseService) {
        this.voucherService = voucherService;
        this.userService = userService;
        this.courseService = courseService;
    }

    // Admin endpoints for voucher management

    @PostMapping("/admin/vouchers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> createVoucher(@Valid @RequestBody VoucherDto voucherDto) {
        VoucherDto createdVoucher = voucherService.createVoucher(voucherDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Voucher created successfully",
                createdVoucher,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/admin/vouchers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getAllVouchers() {
        List<VoucherDto> vouchers = voucherService.getAllVouchers();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "All vouchers retrieved successfully",
                vouchers,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/vouchers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getVoucherById(@PathVariable Long id) {
        VoucherDto voucher = voucherService.getVoucherById(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Voucher retrieved successfully",
                voucher,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/vouchers/code/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getVoucherByCode(@PathVariable String code) {
        VoucherDto voucher = voucherService.getVoucherByCode(code);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Voucher retrieved successfully",
                voucher,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/vouchers/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getActiveVouchers() {
        List<VoucherDto> vouchers = voucherService.getActiveVouchers();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Active vouchers retrieved successfully",
                vouchers,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/vouchers/valid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getValidVouchers() {
        List<VoucherDto> vouchers = voucherService.getValidVouchers();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Valid vouchers retrieved successfully",
                vouchers,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/vouchers/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getVouchersByCourse(@PathVariable Long courseId) {
        List<VoucherDto> vouchers = voucherService.getVouchersByCourse(courseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Vouchers for course retrieved successfully",
                vouchers,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/vouchers/global")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getGlobalVouchers() {
        List<VoucherDto> vouchers = voucherService.getGlobalVouchers();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Global vouchers retrieved successfully",
                vouchers,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/vouchers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> updateVoucher(@PathVariable Long id, @Valid @RequestBody VoucherDto voucherDto) {
        VoucherDto updatedVoucher = voucherService.updateVoucher(id, voucherDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Voucher updated successfully",
                updatedVoucher,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/vouchers/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> activateVoucher(@PathVariable Long id) {
        VoucherDto activatedVoucher = voucherService.activateVoucher(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Voucher activated successfully",
                activatedVoucher,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/vouchers/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> deactivateVoucher(@PathVariable Long id) {
        VoucherDto deactivatedVoucher = voucherService.deactivateVoucher(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Voucher deactivated successfully",
                deactivatedVoucher,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/admin/vouchers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Voucher deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Endpoints for voucher usage management

    @GetMapping("/admin/voucher-usages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getAllVoucherUsages() {
        List<VoucherUsageDto> voucherUsages = voucherService.getVoucherUsages();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "All voucher usages retrieved successfully",
                voucherUsages,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/voucher-usages/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getVoucherUsagesByUser(@PathVariable Long userId) {
        List<VoucherUsageDto> voucherUsages = voucherService.getVoucherUsagesByUser(userId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Voucher usages for user retrieved successfully",
                voucherUsages,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/voucher-usages/voucher/{voucherId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getVoucherUsagesByVoucher(@PathVariable Long voucherId) {
        List<VoucherUsageDto> voucherUsages = voucherService.getVoucherUsagesByVoucher(voucherId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Voucher usages for voucher retrieved successfully",
                voucherUsages,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/voucher-usages/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getVoucherUsagesByCourse(@PathVariable Long courseId) {
        List<VoucherUsageDto> voucherUsages = voucherService.getVoucherUsagesByCourse(courseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Voucher usages for course retrieved successfully",
                voucherUsages,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/admin/voucher-usages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> deleteVoucherUsage(@PathVariable Long id) {
        voucherService.deleteVoucherUsage(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Voucher usage deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Student endpoints for vouchers

    @GetMapping("/student/vouchers/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getValidVouchersForCourse(@PathVariable Long courseId) {
        List<VoucherDto> vouchers = voucherService.getValidVouchersByCourse(courseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Valid vouchers for course retrieved successfully",
                vouchers,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/student/vouchers/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> validateVoucher(@Valid @RequestBody VoucherValidationRequestDto validationDto) {
        boolean isValid = voucherService.isVoucherValid(validationDto.getCode(), validationDto.getCourseId());

        // Get course price and calculate discount if valid
        BigDecimal discount = null;
        BigDecimal originalPrice = null;
        BigDecimal finalPrice = null;

        if (isValid) {
            CourseDto course = courseService.getCourseById(validationDto.getCourseId());
            originalPrice = course.getPrice();

            try {
                discount = voucherService.calculateDiscountAmount(
                        validationDto.getCode(), validationDto.getCourseId(), originalPrice);
                finalPrice = originalPrice.subtract(discount);
            } catch (Exception e) {
                // If calculation fails, voucher is not applicable
                isValid = false;
            }
        }

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                isValid ? "Voucher is valid" : "Voucher is not valid or not applicable",
                Map.of(
                        "valid", isValid,
                        "discount", discount,
                        "originalPrice", originalPrice,
                        "finalPrice", finalPrice
                ),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/student/vouchers/apply")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> applyVoucher(@Valid @RequestBody VoucherApplyRequestDto applyDto, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        // Validate voucher first
        if (!voucherService.isVoucherValid(applyDto.getCode(), applyDto.getCourseId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "Voucher is not valid or not applicable",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }

        // Apply voucher (without payment ID for now)
        VoucherUsageDto voucherUsage = voucherService.applyVoucher(
                applyDto.getCode(), applyDto.getCourseId(), user.getId(), null);

        // Calculate discount
        CourseDto course = courseService.getCourseById(applyDto.getCourseId());
        BigDecimal originalPrice = course.getPrice();
        BigDecimal discount = voucherService.calculateDiscountAmount(
                applyDto.getCode(), applyDto.getCourseId(), originalPrice);
        BigDecimal finalPrice = originalPrice.subtract(discount);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Voucher applied successfully",
                Map.of(
                        "voucherUsage", voucherUsage,
                        "discount", discount,
                        "originalPrice", originalPrice,
                        "finalPrice", finalPrice
                ),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/vouchers/my-usages")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getMyVoucherUsages(Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        List<VoucherUsageDto> voucherUsages = voucherService.getVoucherUsagesByUser(user.getId());

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Your voucher usages retrieved successfully",
                voucherUsages,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}