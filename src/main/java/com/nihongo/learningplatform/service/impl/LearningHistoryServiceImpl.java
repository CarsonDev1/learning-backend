package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.LearningHistoryDto;
import com.nihongo.learningplatform.dto.PageResponseDto;
import com.nihongo.learningplatform.entity.*;
import com.nihongo.learningplatform.repository.*;
import com.nihongo.learningplatform.service.LearningHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LearningHistoryServiceImpl implements LearningHistoryService {

    private final LearningHistoryRepository learningHistoryRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExamRepository examRepository;
    private final MockExamRepository mockExamRepository;
    private final CertificateRepository certificateRepository;
    private final PaymentRepository paymentRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

    public LearningHistoryServiceImpl(
            LearningHistoryRepository learningHistoryRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            LessonRepository lessonRepository,
            ExerciseRepository exerciseRepository,
            ExamRepository examRepository,
            MockExamRepository mockExamRepository,
            CertificateRepository certificateRepository,
            PaymentRepository paymentRepository,
            CommentRepository commentRepository,
            ReviewRepository reviewRepository) {
        this.learningHistoryRepository = learningHistoryRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.exerciseRepository = exerciseRepository;
        this.examRepository = examRepository;
        this.mockExamRepository = mockExamRepository;
        this.certificateRepository = certificateRepository;
        this.paymentRepository = paymentRepository;
        this.commentRepository = commentRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional
    public LearningHistoryDto createLearningHistory(LearningHistoryDto learningHistoryDto) {
        User user = userRepository.findById(learningHistoryDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LearningHistory learningHistory = new LearningHistory();
        learningHistory.setUser(user);
        learningHistory.setActivityType(learningHistoryDto.getActivityType());
        learningHistory.setTimestamp(learningHistoryDto.getTimestamp() != null
                ? learningHistoryDto.getTimestamp() : LocalDateTime.now());
        learningHistory.setDetails(learningHistoryDto.getDetails());

        if (learningHistoryDto.getCourseId() != null) {
            Course course = courseRepository.findById(learningHistoryDto.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            learningHistory.setCourse(course);
        }

        // Save other related entities as needed

        LearningHistory savedHistory = learningHistoryRepository.save(learningHistory);
        return mapToDto(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningHistoryDto getLearningHistoryById(Long id) {
        LearningHistory learningHistory = learningHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning history not found"));
        return mapToDto(learningHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningHistoryDto> getLearningHistoriesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<LearningHistory> histories = learningHistoryRepository.findByUser(user);
        return histories.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<LearningHistoryDto> getLearningHistoriesByUserPaginated(Long userId, int pageNo, int pageSize) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("timestamp").descending());
        Page<LearningHistory> page = learningHistoryRepository.findByUser(user, pageable);

        List<LearningHistoryDto> content = page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PageResponseDto<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningHistoryDto> getLearningHistoriesByUserAndActivityType(Long userId, ActivityType activityType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<LearningHistory> histories = learningHistoryRepository.findByUserAndActivityType(user, activityType);
        return histories.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningHistoryDto> getLearningHistoriesByUserAndCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<LearningHistory> histories = learningHistoryRepository.findByUserAndCourse(user, course);
        return histories.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningHistoryDto> getLearningHistoriesByUserAndDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<LearningHistory> histories = learningHistoryRepository.findByUserAndTimestampBetween(user, start, end);
        return histories.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteOldLearningHistories(LocalDateTime before) {
        // This would require a custom query in the repository
        // For now, we can retrieve and delete individually
        List<LearningHistory> oldHistories = learningHistoryRepository.findAll().stream()
                .filter(history -> history.getTimestamp().isBefore(before))
                .collect(Collectors.toList());

        learningHistoryRepository.deleteAll(oldHistories);
    }

    @Override
    @Transactional
    public void recordCourseEnrollment(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        LearningHistory history = new LearningHistory();
        history.setUser(user);
        history.setCourse(course);
        history.setActivityType(ActivityType.COURSE_ENROLLMENT);
        history.setTimestamp(LocalDateTime.now());
        history.setDetails("Enrolled in course: " + course.getTitle());

        learningHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void recordLessonCompleted(Long userId, Long lessonId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        LearningHistory history = new LearningHistory();
        history.setUser(user);
        history.setActivityType(ActivityType.LESSON_COMPLETED);
        history.setTimestamp(LocalDateTime.now());
        history.setDetails("Completed lesson: " + lesson.getTitle());

        // Set course and module if available
        if (lesson.getModule() != null) {
            history.setModule(lesson.getModule());
            if (lesson.getModule().getCourse() != null) {
                history.setCourse(lesson.getModule().getCourse());
            }
        }

        learningHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void recordExerciseCompleted(Long userId, Long exerciseId, Integer score) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        LearningHistory history = new LearningHistory();
        history.setUser(user);
        history.setActivityType(ActivityType.EXERCISE_COMPLETED);
        history.setTimestamp(LocalDateTime.now());
        history.setDetails("Completed exercise: " + exercise.getTitle() + " with score: " + score);

        // Set related entities
        // Assuming Exercise has references to Lesson, Module, Course

        learningHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void recordExamCompleted(Long userId, Long examId, Integer score, Boolean passed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        LearningHistory history = new LearningHistory();
        history.setUser(user);
        history.setActivityType(ActivityType.EXAM_COMPLETED);
        history.setTimestamp(LocalDateTime.now());
        history.setDetails("Completed exam: " + exam.getTitle() +
                " with score: " + score + ", " + (passed ? "Passed" : "Failed"));

        // Set course if available
        if (exam.getCourse() != null) {
            history.setCourse(exam.getCourse());
        }

        learningHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void recordMockExamCompleted(Long userId, Long mockExamId, Integer score, Boolean passed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MockExam mockExam = mockExamRepository.findById(mockExamId)
                .orElseThrow(() -> new RuntimeException("Mock exam not found"));

        LearningHistory history = new LearningHistory();
        history.setUser(user);
        history.setActivityType(ActivityType.MOCK_EXAM_COMPLETED);
        history.setTimestamp(LocalDateTime.now());
        history.setDetails("Completed mock exam: " + mockExam.getTitle() +
                " with score: " + score + ", " + (passed ? "Passed" : "Failed"));

        learningHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void recordCertificateEarned(Long userId, Long courseId, Long certificateId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        LearningHistory history = new LearningHistory();
        history.setUser(user);
        history.setCourse(course);
        history.setActivityType(ActivityType.CERTIFICATE_EARNED);
        history.setTimestamp(LocalDateTime.now());
        history.setDetails("Earned certificate for course: " + course.getTitle());

        learningHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void recordLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LearningHistory history = new LearningHistory();
        history.setUser(user);
        history.setActivityType(ActivityType.LOGIN);
        history.setTimestamp(LocalDateTime.now());
        history.setDetails("User logged in");

        learningHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void recordPurchase(Long userId, Long paymentId, String details) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        LearningHistory history = new LearningHistory();
        history.setUser(user);
        history.setActivityType(ActivityType.PURCHASE);
        history.setTimestamp(LocalDateTime.now());
        history.setDetails(details != null ? details : "Made a purchase");

        // If payment has an enrollment and enrollment has a course, set the course
        if (payment.getEnrollment() != null && payment.getEnrollment().getCourse() != null) {
            history.setCourse(payment.getEnrollment().getCourse());
        }

        learningHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void recordCommentPosted(Long userId, Long lessonId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        LearningHistory history = new LearningHistory();
        history.setUser(user);
        history.setActivityType(ActivityType.COMMENT_POSTED);
        history.setTimestamp(LocalDateTime.now());
        history.setDetails("Posted a comment on lesson: " + lesson.getTitle());

        // Set module and course if available
        if (lesson.getModule() != null) {
            history.setModule(lesson.getModule());
            if (lesson.getModule().getCourse() != null) {
                history.setCourse(lesson.getModule().getCourse());
            }
        }

        learningHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void recordReviewPosted(Long userId, Long courseId, Long reviewId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        LearningHistory history = new LearningHistory();
        history.setUser(user);
        history.setCourse(course);
        history.setActivityType(ActivityType.REVIEW_POSTED);
        history.setTimestamp(LocalDateTime.now());
        history.setDetails("Posted a review for course: " + course.getTitle());

        learningHistoryRepository.save(history);
    }

    private LearningHistoryDto mapToDto(LearningHistory learningHistory) {
        LearningHistoryDto dto = new LearningHistoryDto();
        dto.setId(learningHistory.getId());
        dto.setUserId(learningHistory.getUser().getId());
        dto.setUsername(learningHistory.getUser().getUsername());
        dto.setActivityType(learningHistory.getActivityType());
        dto.setTimestamp(learningHistory.getTimestamp());
        dto.setDetails(learningHistory.getDetails());

        if (learningHistory.getCourse() != null) {
            dto.setCourseId(learningHistory.getCourse().getId());
            dto.setCourseName(learningHistory.getCourse().getTitle());
        }

        if (learningHistory.getModule() != null) {
            dto.setModuleId(learningHistory.getModule().getId());
            dto.setModuleName(learningHistory.getModule().getTitle());
        }

        // Set other fields as needed based on your entity relationships

        return dto;
    }
}