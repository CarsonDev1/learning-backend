package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.CloudinaryResponseDto;
import com.nihongo.learningplatform.dto.FileUploadDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudinaryService {
    CloudinaryResponseDto uploadFile(FileUploadDto fileUploadDto);
    CloudinaryResponseDto uploadFile(MultipartFile file, String resourceType);
    List<CloudinaryResponseDto> uploadMultipleFiles(List<MultipartFile> files, String resourceType);
    boolean deleteFile(String publicId, String resourceType);
    CloudinaryResponseDto getFileInfo(String publicId);
}