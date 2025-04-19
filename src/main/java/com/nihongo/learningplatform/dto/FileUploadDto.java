package com.nihongo.learningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDto {
    private MultipartFile file;
    private String resourceType; // "image", "video", "raw", etc.
    private String folder; // Optional: folder in Cloudinary
    private String publicId; // Optional: custom public ID
}

