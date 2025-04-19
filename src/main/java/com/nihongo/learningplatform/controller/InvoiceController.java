
package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.ApiResponseDto;
import com.nihongo.learningplatform.dto.InvoiceDto;
import com.nihongo.learningplatform.entity.InvoiceStatus;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.InvoiceService;
import com.nihongo.learningplatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final UserService userService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService, UserService userService) {
        this.invoiceService = invoiceService;
        this.userService = userService;
    }

    // Student endpoints

    @GetMapping("/student/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getUserInvoices(Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        List<InvoiceDto> invoices = invoiceService.getInvoicesByUser(user.getId());

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Your invoices retrieved successfully",
                invoices,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/invoices/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getUserInvoiceById(@PathVariable Long id, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        InvoiceDto invoice = invoiceService.getInvoiceById(id);

        // Check if the invoice belongs to the user or the user is an admin
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        if (!isAdmin && !invoice.getUserId().equals(user.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You do not have permission to view this invoice",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Invoice retrieved successfully",
                invoice,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/invoices/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> downloadInvoice(@PathVariable Long id, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        InvoiceDto invoice = invoiceService.getInvoiceById(id);

        // Check if the invoice belongs to the user or the user is an admin
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        if (!isAdmin && !invoice.getUserId().equals(user.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You do not have permission to download this invoice",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        String pdfUrl = invoiceService.generateInvoicePdf(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Invoice PDF generated successfully",
                pdfUrl,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Admin endpoints

    @PostMapping("/admin/invoices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> createInvoice(@Valid @RequestBody InvoiceDto invoiceDto) {
        // Generate invoice number if not provided
        if (invoiceDto.getInvoiceNumber() == null || invoiceDto.getInvoiceNumber().isEmpty()) {
            invoiceDto.setInvoiceNumber(invoiceService.generateInvoiceNumber());
        }

        // Set issued date if not provided
        if (invoiceDto.getIssuedDate() == null) {
            invoiceDto.setIssuedDate(LocalDateTime.now());
        }

        InvoiceDto createdInvoice = invoiceService.createInvoice(invoiceDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Invoice created successfully",
                createdInvoice,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/admin/invoices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getAllInvoices() {
        List<InvoiceDto> invoices = invoiceService.getAllInvoices();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "All invoices retrieved successfully",
                invoices,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/invoices/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getInvoicesByStatus(@PathVariable InvoiceStatus status) {
        List<InvoiceDto> invoices = invoiceService.getInvoicesByStatus(status);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Invoices with status " + status + " retrieved successfully",
                invoices,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/payments/{paymentId}/invoice")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getInvoiceByPayment(@PathVariable Long paymentId) {
        InvoiceDto invoice = invoiceService.getInvoiceByPayment(paymentId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Invoice for payment retrieved successfully",
                invoice,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/invoices/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> updateInvoice(@PathVariable Long id,
                                                        @Valid @RequestBody InvoiceDto invoiceDto) {
        invoiceDto.setId(id);
        InvoiceDto updatedInvoice = invoiceService.updateInvoice(id, invoiceDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Invoice updated successfully",
                updatedInvoice,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/invoices/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> updateInvoiceStatus(@PathVariable Long id,
                                                              @PathVariable InvoiceStatus status) {
        InvoiceDto updatedInvoice = invoiceService.updateInvoiceStatus(id, status);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Invoice status updated successfully",
                updatedInvoice,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/admin/invoices/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Invoice deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}