package com.nihongo.learningplatform.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import com.nihongo.learningplatform.dto.CloudinaryResponseDto;
import com.nihongo.learningplatform.dto.FileUploadDto;
import com.nihongo.learningplatform.exception.BadRequestException;
import com.nihongo.learningplatform.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public CloudinaryResponseDto uploadFile(FileUploadDto fileUploadDto) {
        return uploadFile(fileUploadDto.getFile(), fileUploadDto.getResourceType(),
                fileUploadDto.getFolder(), fileUploadDto.getPublicId());
    }

    @Override
    public CloudinaryResponseDto uploadFile(MultipartFile file, String resourceType) {
        return uploadFile(file, resourceType, null, null);
    }

    private CloudinaryResponseDto uploadFile(MultipartFile file, String resourceType,
                                             String folder, String publicId) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        try {
            // Set up upload parameters
            Map<String, Object> params = ObjectUtils.asMap(
                    "resource_type", resourceType != null ? resourceType : "auto",
                    "unique_filename", true
            );

            if (folder != null && !folder.isEmpty()) {
                params.put("folder", folder);
            }

            if (publicId != null && !publicId.isEmpty()) {
                params.put("public_id", publicId);
            }

            // Upload file to Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            // Map Cloudinary response to our DTO
            return mapToCloudinaryResponseDto(uploadResult, file.getOriginalFilename());

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }

    @Override
    public List<CloudinaryResponseDto> uploadMultipleFiles(List<MultipartFile> files, String resourceType) {
        List<CloudinaryResponseDto> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            responses.add(uploadFile(file, resourceType));
        }

        return responses;
    }

    @Override
    public boolean deleteFile(String publicId, String resourceType) {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                    "resource_type", resourceType != null ? resourceType : "image"
            );

            Map<String, Object> result = cloudinary.uploader().destroy(publicId, params);
            return "ok".equals(result.get("result"));

        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from Cloudinary", e);
        }
    }

    @Override
    public CloudinaryResponseDto getFileInfo(String publicId) {
        try {
            ApiResponse response = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());

            CloudinaryResponseDto dto = new CloudinaryResponseDto();
            dto.setPublicId(response.get("public_id").toString());
            dto.setUrl(response.get("url").toString());
            dto.setSecureUrl(response.get("secure_url").toString());
            dto.setResourceType(response.get("resource_type").toString());
            dto.setFormat(response.get("format").toString());
            dto.setBytes(Long.parseLong(response.get("bytes").toString()));

            if (response.containsKey("width")) {
                dto.setWidth(Integer.parseInt(response.get("width").toString()));
            }

            if (response.containsKey("height")) {
                dto.setHeight(Integer.parseInt(response.get("height").toString()));
            }

            return dto;

        } catch (Exception e) {
            CloudinaryResponseDto errorResponse = new CloudinaryResponseDto();
            errorResponse.setError("File not found or error retrieving information: " + e.getMessage());
            return errorResponse;
        }
    }

    // Helper method to map Cloudinary response to our DTO
    private CloudinaryResponseDto mapToCloudinaryResponseDto(Map<String, Object> result, String originalFilename) {
        CloudinaryResponseDto response = new CloudinaryResponseDto();
        response.setPublicId((String) result.get("public_id"));
        response.setUrl((String) result.get("url"));
        response.setSecureUrl((String) result.get("secure_url"));
        response.setResourceType((String) result.get("resource_type"));
        response.setFormat((String) result.get("format"));
        response.setBytes(((Number) result.get("bytes")).longValue());
        response.setOriginalFilename(originalFilename);

        if (result.containsKey("width")) {
            response.setWidth(((Number) result.get("width")).intValue());
        }

        if (result.containsKey("height")) {
            response.setHeight(((Number) result.get("height")).intValue());
        }

        return response;
    }
}