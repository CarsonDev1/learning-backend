package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.ReviewDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Review;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.ReviewRepository;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.ReviewService;
import com.nihongo.learningplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final CourseService courseService;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             UserService userService,
                             CourseService courseService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.courseService = courseService;
    }

    @Override
    @Transactional
    public ReviewDto createReview(ReviewDto reviewDto) {
        User user = userService.getUserEntityById(reviewDto.getUserId());
        Course course = courseService.getCourseEntityById(reviewDto.getCourseId());

        // Check if user already reviewed this course
        Optional<Review> existingReview = reviewRepository.findByUserAndCourse(user, course);
        if (existingReview.isPresent()) {
            // Update existing review
            Review review = existingReview.get();
            review.setRating(reviewDto.getRating());
            review.setComment(reviewDto.getComment());
            Review updatedReview = reviewRepository.save(review);
            return mapToDto(updatedReview);
        }

        // Create new review
        Review review = new Review();
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        review.setUser(user);
        review.setCourse(course);

        Review savedReview = reviewRepository.save(review);
        return mapToDto(savedReview);
    }

    @Override
    public ReviewDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        return mapToDto(review);
    }

    @Override
    public List<ReviewDto> getReviewsByCourse(Long courseId) {
        Course course = courseService.getCourseEntityById(courseId);
        List<Review> reviews = reviewRepository.findByCourse(course);
        return reviews.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDto getReviewByUserAndCourse(Long userId, Long courseId) {
        User user = userService.getUserEntityById(userId);
        Course course = courseService.getCourseEntityById(courseId);

        Review review = reviewRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for user id: " +
                        userId + " and course id: " + courseId));

        return mapToDto(review);
    }

    @Override
    public List<ReviewDto> getReviewsByUser(Long userId) {
        User user = userService.getUserEntityById(userId);
        List<Review> reviews = reviewRepository.findByUser(user);
        return reviews.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewDto updateReview(Long id, ReviewDto reviewDto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        Review updatedReview = reviewRepository.save(review);
        return mapToDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    // Helper method to map Review entity to ReviewDto
    private ReviewDto mapToDto(Review review) {
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setId(review.getId());
        reviewDto.setRating(review.getRating());
        reviewDto.setComment(review.getComment());
        reviewDto.setCreatedAt(review.getCreatedAt());
        reviewDto.setUserId(review.getUser().getId());
        reviewDto.setCourseId(review.getCourse().getId());
        reviewDto.setUsername(review.getUser().getUsername());
        return reviewDto;
    }
}