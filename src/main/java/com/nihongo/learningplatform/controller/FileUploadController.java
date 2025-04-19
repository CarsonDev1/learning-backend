package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.ApiResponseDto;
import com.nihongo.learningplatform.dto.CloudinaryResponseDto;
import com.nihongo.learningplatform.dto.FileUploadDto;
import com.nihongo.learningplatform.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    @Autowired
    public FileUploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> uploadFile(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(value = "resourceType", defaultValue = "auto") String resourceType,
                                                     @RequestParam(value = "folder", required = false) String folder) {

        FileUploadDto uploadDto = new FileUploadDto();
        uploadDto.setFile(file);
        uploadDto.setResourceType(resourceType);
        uploadDto.setFolder(folder);

        CloudinaryResponseDto response = cloudinaryService.uploadFile(uploadDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "File uploaded successfully",
                response,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> uploadMultipleFiles(@RequestParam("files") List<MultipartFile> files,
                                                              @RequestParam(value = "resourceType", defaultValue = "auto") String resourceType) {

        List<CloudinaryResponseDto> responses = cloudinaryService.uploadMultipleFiles(files, resourceType);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Files uploaded successfully",
                responses,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> deleteFile(@RequestParam("publicId") String publicId,
                                                     @RequestParam(value = "resourceType", defaultValue = "image") String resourceType) {

        boolean deleted = cloudinaryService.deleteFile(publicId, resourceType);

        ApiResponseDto apiResponse = new ApiResponseDto(
                deleted,
                deleted ? "File deleted successfully" : "Failed to delete file",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/info/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getFileInfo(@PathVariable String publicId) {

        CloudinaryResponseDto info = cloudinaryService.getFileInfo(publicId);

        boolean success = info.getError() == null;

        ApiResponseDto apiResponse = new ApiResponseDto(
                success,
                success ? "File information retrieved successfully" : info.getError(),
                success ? info : null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}