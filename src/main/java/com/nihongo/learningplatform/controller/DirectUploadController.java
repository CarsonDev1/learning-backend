package com.nihongo.learningplatform.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nihongo.learningplatform.dto.ApiResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class DirectUploadController {

    private final Cloudinary cloudinary;

    @Autowired
    public DirectUploadController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @GetMapping("/signature")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> getUploadSignature(
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "publicId", required = false) String publicId) {

        try {
            // Thiết lập parameters
            Map<String, Object> params = new HashMap<>();
            params.put("timestamp", System.currentTimeMillis() / 1000);

            if (folder != null && !folder.isEmpty()) {
                params.put("folder", folder);
            }

            if (publicId != null && !publicId.isEmpty()) {
                params.put("public_id", publicId);
            }

            // Tạo chữ ký
            String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

            // Trả về thông tin cần thiết cho frontend
            Map<String, Object> response = new HashMap<>();
            response.put("signature", signature);
            response.put("timestamp", params.get("timestamp"));
            response.put("cloudName", cloudinary.config.cloudName);
            response.put("apiKey", cloudinary.config.apiKey);

            if (folder != null) {
                response.put("folder", folder);
            }

            if (publicId != null) {
                response.put("publicId", publicId);
            }

            ApiResponseDto apiResponse = new ApiResponseDto(
                    true,
                    "Upload signature generated successfully",
                    response,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "Failed to generate upload signature: " + e.getMessage(),
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}