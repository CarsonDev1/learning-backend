package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.InvoiceDto;
import com.nihongo.learningplatform.entity.Invoice;
import com.nihongo.learningplatform.entity.InvoiceStatus;
import com.nihongo.learningplatform.entity.Payment;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.InvoiceRepository;
import com.nihongo.learningplatform.service.InvoiceService;
import com.nihongo.learningplatform.service.PaymentService;
import com.nihongo.learningplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserService userService;
    private final PaymentService paymentService;

    @Value("${app.file.invoice-template}")
    private String invoiceTemplatePath;

    @Autowired
    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              UserService userService,
                              PaymentService paymentService) {
        this.invoiceRepository = invoiceRepository;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional
    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {
        User user = userService.getUserEntityById(invoiceDto.getUserId());

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceDto.getInvoiceNumber() != null ?
                invoiceDto.getInvoiceNumber() : generateInvoiceNumber());
        invoice.setIssuedDate(invoiceDto.getIssuedDate() != null ?
                invoiceDto.getIssuedDate() : LocalDateTime.now());
        invoice.setAmount(invoiceDto.getAmount());
        invoice.setTaxAmount(invoiceDto.getTaxAmount());
        invoice.setTotalAmount(invoiceDto.getTotalAmount());
        invoice.setCustomerName(invoiceDto.getCustomerName());
        invoice.setCustomerEmail(invoiceDto.getCustomerEmail());
        invoice.setCustomerAddress(invoiceDto.getCustomerAddress());
        invoice.setCustomerTaxId(invoiceDto.getCustomerTaxId());
        invoice.setStatus(invoiceDto.getStatus());
        invoice.setUser(user);

        // Link to payment if provided
        if (invoiceDto.getPaymentId() != null) {
            Payment payment = paymentService.getPaymentEntityById(invoiceDto.getPaymentId());
            invoice.setPayment(payment);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToDto(savedInvoice);
    }

    @Override
    public InvoiceDto getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        return mapToDto(invoice);
    }

    @Override
    public InvoiceDto getInvoiceByInvoiceNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with number: " + invoiceNumber));
        return mapToDto(invoice);
    }

    @Override
    public List<InvoiceDto> getInvoicesByUser(Long userId) {
        User user = userService.getUserEntityById(userId);
        List<Invoice> invoices = invoiceRepository.findByUser(user);
        return invoices.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDto> getInvoicesByStatus(InvoiceStatus status) {
        List<Invoice> invoices = invoiceRepository.findByStatus(status);
        return invoices.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceDto getInvoiceByPayment(Long paymentId) {
        Payment payment = paymentService.getPaymentEntityById(paymentId);
        Invoice invoice = invoiceRepository.findByPayment(payment)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for payment id: " + paymentId));
        return mapToDto(invoice);
    }

    @Override
    @Transactional
    public InvoiceDto updateInvoice(Long id, InvoiceDto invoiceDto) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        invoice.setAmount(invoiceDto.getAmount());
        invoice.setTaxAmount(invoiceDto.getTaxAmount());
        invoice.setTotalAmount(invoiceDto.getTotalAmount());
        invoice.setCustomerName(invoiceDto.getCustomerName());
        invoice.setCustomerEmail(invoiceDto.getCustomerEmail());
        invoice.setCustomerAddress(invoiceDto.getCustomerAddress());
        invoice.setCustomerTaxId(invoiceDto.getCustomerTaxId());
        invoice.setStatus(invoiceDto.getStatus());

        // Update payment link if changed
        if (invoiceDto.getPaymentId() != null &&
                (invoice.getPayment() == null || !invoice.getPayment().getId().equals(invoiceDto.getPaymentId()))) {
            Payment payment = paymentService.getPaymentEntityById(invoiceDto.getPaymentId());
            invoice.setPayment(payment);
        }

        Invoice updatedInvoice = invoiceRepository.save(invoice);
        return mapToDto(updatedInvoice);
    }

    @Override
    @Transactional
    public InvoiceDto updateInvoiceStatus(Long id, InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        invoice.setStatus(status);
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        return mapToDto(updatedInvoice);
    }

    @Override
    @Transactional
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + id);
        }
        invoiceRepository.deleteById(id);
    }

    @Override
    public Invoice getInvoiceEntityById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }

    @Override
    public String generateInvoiceNumber() {
        // Format: INV-YYYYMMDD-XXXX where XXXX is a random 4-digit number
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%04d", (int) (Math.random() * 10000));
        return "INV-" + datePrefix + "-" + randomSuffix;
    }

    @Override
    public String generateInvoicePdf(Long invoiceId) {
        // Get invoice
        Invoice invoice = getInvoiceEntityById(invoiceId);

        try {
            // In a real implementation, you would use a PDF library like iText, PDFBox, or JasperReports
            // to generate the PDF based on the invoice template and data

            // For this example, we'll just simulate creating a PDF and return a file path
            String fileName = "invoice_" + invoice.getInvoiceNumber() + ".pdf";
            String filePath = "/generated/invoices/" + fileName;

            // Here would be the actual PDF generation code
            // Example: PDFGenerator.generateInvoicePdf(invoice, invoiceTemplatePath, filePath);

            return filePath;
        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public List<InvoiceDto> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll();
        return invoices.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Helper method to map Invoice entity to InvoiceDto
    private InvoiceDto mapToDto(Invoice invoice) {
        InvoiceDto invoiceDto = new InvoiceDto();
        invoiceDto.setId(invoice.getId());
        invoiceDto.setInvoiceNumber(invoice.getInvoiceNumber());
        invoiceDto.setIssuedDate(invoice.getIssuedDate());
        invoiceDto.setAmount(invoice.getAmount());
        invoiceDto.setTaxAmount(invoice.getTaxAmount());
        invoiceDto.setTotalAmount(invoice.getTotalAmount());
        invoiceDto.setCustomerName(invoice.getCustomerName());
        invoiceDto.setCustomerEmail(invoice.getCustomerEmail());
        invoiceDto.setCustomerAddress(invoice.getCustomerAddress());
        invoiceDto.setCustomerTaxId(invoice.getCustomerTaxId());
        invoiceDto.setStatus(invoice.getStatus());
        invoiceDto.setPdfUrl(invoice.getPdfUrl());
        invoiceDto.setUserId(invoice.getUser().getId());
        invoiceDto.setUsername(invoice.getUser().getUsername());

        if (invoice.getPayment() != null) {
            invoiceDto.setPaymentId(invoice.getPayment().getId());
        }

        return invoiceDto;
    }
}