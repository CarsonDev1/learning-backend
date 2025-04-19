package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.CertificateDto;
import com.nihongo.learningplatform.entity.Certificate;

import java.util.List;

public interface CertificateService {
    CertificateDto createCertificate(CertificateDto certificateDto);
    CertificateDto getCertificateById(Long id);
    CertificateDto getCertificateByCertificateNumber(String certificateNumber);
    List<CertificateDto> getCertificatesByUser(Long userId);
    List<CertificateDto> getCertificatesByCourse(Long courseId);
    CertificateDto getCertificateByUserAndCourse(Long userId, Long courseId);

    List<CertificateDto> getAllCertificates();

    CertificateDto updateCertificate(Long id, CertificateDto certificateDto);
    void deleteCertificate(Long id);
    Certificate getCertificateEntityById(Long id);
    String generateCertificateNumber();
    String generateCertificatePdf(Long certificateId);
}
