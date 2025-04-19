package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.ApiResponseDto;
import com.nihongo.learningplatform.dto.LearningHistoryDto;
import com.nihongo.learningplatform.dto.PageResponseDto;
import com.nihongo.learningplatform.entity.ActivityType;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.LearningHistoryService;
import com.nihongo.learningplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class LearningHistoryController {

    private final LearningHistoryService learningHistoryService;
    private final UserService userService;

    @Autowired
    public LearningHistoryController(LearningHistoryService learningHistoryService, UserService userService) {
        this.learningHistoryService = learningHistoryService;
        this.userService = userService;
    }

    // Student endpoints

    @GetMapping("/student/learning-history")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getUserLearningHistory(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        PageResponseDto<LearningHistoryDto> history =
                learningHistoryService.getLearningHistoriesByUserPaginated(user.getId(), pageNo, pageSize);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Your learning history retrieved successfully",
                history,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/learning-history/type/{activityType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getUserLearningHistoryByType(
            @PathVariable ActivityType activityType, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        List<LearningHistoryDto> history =
                learningHistoryService.getLearningHistoriesByUserAndActivityType(user.getId(), activityType);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Your learning history for activity type " + activityType + " retrieved successfully",
                history,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/courses/{courseId}/learning-history")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getUserLearningHistoryByCourse(
            @PathVariable Long courseId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        List<LearningHistoryDto> history =
                learningHistoryService.getLearningHistoriesByUserAndCourse(user.getId(), courseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Your learning history for this course retrieved successfully",
                history,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/student/learning-history/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getUserLearningHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());
        List<LearningHistoryDto> history =
                learningHistoryService.getLearningHistoriesByUserAndDateRange(user.getId(), start, end);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Your learning history between " + start + " and " + end + " retrieved successfully",
                history,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Admin endpoints

    @GetMapping("/admin/users/{userId}/learning-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getUserLearningHistoryByAdmin(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResponseDto<LearningHistoryDto> history =
                learningHistoryService.getLearningHistoriesByUserPaginated(userId, pageNo, pageSize);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Learning history for user retrieved successfully",
                history,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/admin/learning-history/delete-old")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> deleteOldLearningHistories(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before) {
        learningHistoryService.deleteOldLearningHistories(before);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Old learning histories deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}