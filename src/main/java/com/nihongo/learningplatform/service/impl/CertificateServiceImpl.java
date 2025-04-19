package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.CertificateDto;
import com.nihongo.learningplatform.entity.Certificate;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.CertificateRepository;
import com.nihongo.learningplatform.service.CertificateService;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserService userService;
    private final CourseService courseService;

    @Value("${app.file.certificate-template:./templates/certificate-template.pdf}")
    private String certificateTemplatePath;

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${certificate.issuer.name:Japanese Learning Platform}")
    private String certificateIssuerName;

    @Value("${certificate.issuer.signature:Admin Signature}")
    private String certificateIssuerSignature;

    @Autowired
    public CertificateServiceImpl(CertificateRepository certificateRepository,
                                  UserService userService,
                                  CourseService courseService) {
        this.certificateRepository = certificateRepository;
        this.userService = userService;
        this.courseService = courseService;
    }

    @Override
    @Transactional
    public CertificateDto createCertificate(CertificateDto certificateDto) {
        User user = userService.getUserEntityById(certificateDto.getUserId());
        Course course = courseService.getCourseEntityById(certificateDto.getCourseId());

        // Check if certificate already exists for this user and course
        if (certificateRepository.findByUserAndCourse(user, course).isPresent()) {
            throw new IllegalStateException("Certificate already exists for this user and course");
        }

        Certificate certificate = new Certificate();
        certificate.setTitle(certificateDto.getTitle());
        certificate.setCertificateNumber(certificateDto.getCertificateNumber() != null ?
                certificateDto.getCertificateNumber() : generateCertificateNumber());
        certificate.setDescription(certificateDto.getDescription());
        certificate.setIssuedDate(certificateDto.getIssuedDate() != null ?
                certificateDto.getIssuedDate() : LocalDateTime.now());
        certificate.setUser(user);
        certificate.setCourse(course);
        certificate.setFinalScore(certificateDto.getFinalScore());

        Certificate savedCertificate = certificateRepository.save(certificate);

        // Generate PDF in background if not provided
        if (certificateDto.getPdfUrl() == null || certificateDto.getPdfUrl().isEmpty()) {
            String pdfUrl = generateCertificatePdf(savedCertificate.getId());
            savedCertificate.setPdfUrl(pdfUrl);
            savedCertificate = certificateRepository.save(savedCertificate);
        } else {
            savedCertificate.setPdfUrl(certificateDto.getPdfUrl());
            savedCertificate = certificateRepository.save(savedCertificate);
        }

        // Generate image in background if not provided
        if (certificateDto.getImageUrl() == null || certificateDto.getImageUrl().isEmpty()) {
            // Generate image from PDF would be implemented here
            String imageUrl = "/certificates/images/" + savedCertificate.getCertificateNumber() + ".jpg";
            savedCertificate.setImageUrl(imageUrl);
            savedCertificate = certificateRepository.save(savedCertificate);
        } else {
            savedCertificate.setImageUrl(certificateDto.getImageUrl());
            savedCertificate = certificateRepository.save(savedCertificate);
        }

        return mapToDto(savedCertificate);
    }

    @Override
    public CertificateDto getCertificateById(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));
        return mapToDto(certificate);
    }

    @Override
    public CertificateDto getCertificateByCertificateNumber(String certificateNumber) {
        Certificate certificate = certificateRepository.findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with number: " + certificateNumber));
        return mapToDto(certificate);
    }

    @Override
    public List<CertificateDto> getCertificatesByUser(Long userId) {
        User user = userService.getUserEntityById(userId);
        List<Certificate> certificates = certificateRepository.findByUser(user);
        return certificates.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CertificateDto> getCertificatesByCourse(Long courseId) {
        Course course = courseService.getCourseEntityById(courseId);
        List<Certificate> certificates = certificateRepository.findByCourse(course);
        return certificates.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CertificateDto getCertificateByUserAndCourse(Long userId, Long courseId) {
        User user = userService.getUserEntityById(userId);
        Course course = courseService.getCourseEntityById(courseId);
        Certificate certificate = certificateRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found for user id: " +
                        userId + " and course id: " + courseId));
        return mapToDto(certificate);
    }

    @Override
    public List<CertificateDto> getAllCertificates() {
        List<Certificate> certificates = certificateRepository.findAll();
        return certificates.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CertificateDto updateCertificate(Long id, CertificateDto certificateDto) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));

        certificate.setTitle(certificateDto.getTitle());
        certificate.setDescription(certificateDto.getDescription());
        certificate.setFinalScore(certificateDto.getFinalScore());

        // Only admin can change certificate number
        if (certificateDto.getCertificateNumber() != null && !certificateDto.getCertificateNumber().isEmpty()) {
            certificate.setCertificateNumber(certificateDto.getCertificateNumber());
        }

        // Only admin can change issued date
        if (certificateDto.getIssuedDate() != null) {
            certificate.setIssuedDate(certificateDto.getIssuedDate());
        }

        // Update PDF URL if provided
        if (certificateDto.getPdfUrl() != null && !certificateDto.getPdfUrl().isEmpty()) {
            certificate.setPdfUrl(certificateDto.getPdfUrl());
        }

        // Update image URL if provided
        if (certificateDto.getImageUrl() != null && !certificateDto.getImageUrl().isEmpty()) {
            certificate.setImageUrl(certificateDto.getImageUrl());
        }

        Certificate updatedCertificate = certificateRepository.save(certificate);
        return mapToDto(updatedCertificate);
    }

    @Override
    @Transactional
    public void deleteCertificate(Long id) {
        if (!certificateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Certificate not found with id: " + id);
        }
        certificateRepository.deleteById(id);
    }

    @Override
    public Certificate getCertificateEntityById(Long id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));
    }

    @Override
    public String generateCertificateNumber() {
        String prefix = "CERT";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return prefix + "-" + timestamp + "-" + uniquePart;
    }

    @Override
    public String generateCertificatePdf(Long certificateId) {
        // This would normally generate a PDF using a library like iText, PDFBox, etc.
        // For this example, we'll just return a placeholder URL

        Certificate certificate = getCertificateEntityById(certificateId);

        // Create certificates directory if it doesn't exist
        File certificatesDir = new File(uploadDir + "/certificates");
        if (!certificatesDir.exists()) {
            certificatesDir.mkdirs();
        }

        // The PDF would be generated here
        // PDFGenerator.generate(certificateTemplatePath, certificate, outputPath);

        // Return the URL to the PDF
        String pdfUrl = "/certificates/" + certificate.getCertificateNumber() + ".pdf";

        // Update the certificate with the PDF URL
        certificate.setPdfUrl(pdfUrl);
        certificateRepository.save(certificate);

        return pdfUrl;
    }

    // Helper method to map Certificate entity to CertificateDto
    private CertificateDto mapToDto(Certificate certificate) {
        CertificateDto certificateDto = new CertificateDto();
        certificateDto.setId(certificate.getId());
        certificateDto.setCertificateNumber(certificate.getCertificateNumber());
        certificateDto.setTitle(certificate.getTitle());
        certificateDto.setDescription(certificate.getDescription());
        certificateDto.setIssuedDate(certificate.getIssuedDate());
        certificateDto.setPdfUrl(certificate.getPdfUrl());
        certificateDto.setImageUrl(certificate.getImageUrl());
        certificateDto.setUserId(certificate.getUser().getId());
        certificateDto.setUsername(certificate.getUser().getUsername());
        certificateDto.setCourseId(certificate.getCourse().getId());
        certificateDto.setCourseName(certificate.getCourse().getTitle());
        certificateDto.setFinalScore(certificate.getFinalScore());
        certificateDto.setCreatedAt(certificate.getCreatedAt());
        return certificateDto;
    }
}