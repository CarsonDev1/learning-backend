package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.InvoiceDto;
import com.nihongo.learningplatform.entity.Invoice;
import com.nihongo.learningplatform.entity.InvoiceStatus;

import java.util.List;

public interface InvoiceService {
    InvoiceDto createInvoice(InvoiceDto invoiceDto);
    InvoiceDto getInvoiceById(Long id);
    InvoiceDto getInvoiceByInvoiceNumber(String invoiceNumber);
    List<InvoiceDto> getInvoicesByUser(Long userId);
    List<InvoiceDto> getInvoicesByStatus(InvoiceStatus status);
    InvoiceDto getInvoiceByPayment(Long paymentId);
    InvoiceDto updateInvoice(Long id, InvoiceDto invoiceDto);
    InvoiceDto updateInvoiceStatus(Long id, InvoiceStatus status);
    void deleteInvoice(Long id);
    Invoice getInvoiceEntityById(Long id);
    String generateInvoiceNumber();
    String generateInvoicePdf(Long invoiceId);

    List<InvoiceDto> getAllInvoices();
}