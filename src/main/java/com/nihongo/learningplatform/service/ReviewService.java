package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.ReviewDto;
import com.nihongo.learningplatform.entity.Review;

import java.util.List;

public interface ReviewService {
    ReviewDto createReview(ReviewDto reviewDto);
    ReviewDto getReviewById(Long id);
    List<ReviewDto> getReviewsByCourse(Long courseId);
    ReviewDto getReviewByUserAndCourse(Long userId, Long courseId);
    List<ReviewDto> getReviewsByUser(Long userId);
    ReviewDto updateReview(Long id, ReviewDto reviewDto);
    void deleteReview(Long id);
}
