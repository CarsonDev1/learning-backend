package com.nihongo.learningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloudinaryResponseDto {
    private String publicId;
    private String url;
    private String secureUrl;
    private String resourceType;
    private String format;
    private int width; // For images and videos
    private int height; // For images and videos
    private long bytes;
    private String originalFilename;
    private String error; // For error responses
}
