package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.CourseDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Review;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.CourseRepository;
import com.nihongo.learningplatform.repository.ReviewRepository;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.ExamService;
import com.nihongo.learningplatform.service.ExerciseService;
import com.nihongo.learningplatform.entity.Exercise;
import com.nihongo.learningplatform.entity.Exam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final ReviewRepository reviewRepository;
    private final ExerciseService exerciseService;
    private final ExamService examService;

    @Autowired
    public CourseServiceImpl(CourseRepository courseRepository,
                             ReviewRepository reviewRepository,
                             @Lazy ExerciseService exerciseService,
                             @Lazy ExamService examService) {
        this.courseRepository = courseRepository;
        this.reviewRepository = reviewRepository;
        this.exerciseService = exerciseService;
        this.examService = examService;
    }




    @Override
    @Transactional
    public CourseDto createCourse(CourseDto courseDto, User instructor) {
        Course course = new Course();
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setPrice(courseDto.getPrice());
        course.setThumbnailUrl(courseDto.getThumbnailUrl());
        course.setLevel(courseDto.getLevel());
        course.setInstructor(instructor);
        course.setApproved(false); // New courses need approval
        course.setActive(true);

        Course savedCourse = courseRepository.save(course);
        return mapToDtoWithStats(savedCourse);
    }

    @Override
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return mapToDtoWithStats(course);
    }

    @Override
    public List<CourseDto> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                .map(this::mapToDtoWithStats)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDto> getCoursesByInstructor(User instructor) {
        List<Course> courses = courseRepository.findByInstructor(instructor);
        return courses.stream()
                .map(this::mapToDtoWithStats)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDto> getApprovedAndActiveCourses() {
        List<Course> courses = courseRepository.findByApprovedAndActive(true, true);
        return courses.stream()
                .map(this::mapToDtoWithStats)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDto> getUnapprovedCourses() {
        List<Course> courses = courseRepository.findByApproved(false);
        return courses.stream()
                .map(this::mapToDtoWithStats)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDto> getCoursesByLevel(String level) {
        List<Course> courses = courseRepository.findByLevel(level);
        return courses.stream()
                .map(this::mapToDtoWithStats)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDto> searchCourses(String keyword) {
        List<Course> courses = courseRepository.searchCourses(keyword);
        return courses.stream()
                .map(this::mapToDtoWithStats)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourseDto updateCourse(Long id, CourseDto courseDto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setPrice(courseDto.getPrice());
        course.setThumbnailUrl(courseDto.getThumbnailUrl());
        course.setLevel(courseDto.getLevel());

        // After updating a course, it should be reviewed again
        course.setApproved(false);

        Course updatedCourse = courseRepository.save(course);
        return mapToDtoWithStats(updatedCourse);
    }

    @Override
    @Transactional
    public CourseDto approveCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        course.setApproved(true);
        Course updatedCourse = courseRepository.save(course);
        return mapToDtoWithStats(updatedCourse);
    }

    @Override
    @Transactional
    public CourseDto rejectCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        course.setApproved(false);
        Course updatedCourse = courseRepository.save(course);
        return mapToDtoWithStats(updatedCourse);
    }

    @Override
    @Transactional
    public CourseDto activateCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        course.setActive(true);
        Course updatedCourse = courseRepository.save(course);
        return mapToDtoWithStats(updatedCourse);
    }

    @Override
    @Transactional
    public CourseDto deactivateCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        course.setActive(false);
        Course updatedCourse = courseRepository.save(course);
        return mapToDtoWithStats(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    @Override
    public Course getCourseEntityById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    @Override
    public Double getAverageRating(Long courseId) {
        Course course = getCourseEntityById(courseId);
        return reviewRepository.getAverageRatingForCourse(course);
    }

    @Override
    public int getReviewCount(Long courseId) {
        Course course = getCourseEntityById(courseId);
        List<Review> reviews = reviewRepository.findByCourse(course);
        return reviews.size();
    }

    // Helper method to map Course entity to CourseDto
    private CourseDto mapToDto(Course course) {
        CourseDto courseDto = new CourseDto();
        courseDto.setId(course.getId());
        courseDto.setTitle(course.getTitle());
        courseDto.setDescription(course.getDescription());
        courseDto.setPrice(course.getPrice());
        courseDto.setThumbnailUrl(course.getThumbnailUrl());
        courseDto.setApproved(course.isApproved());
        courseDto.setActive(course.isActive());
        courseDto.setLevel(course.getLevel());
        courseDto.setInstructorId(course.getInstructor().getId());
        courseDto.setInstructorName(course.getInstructor().getFullName());
        return courseDto;
    }

    // Helper method to map Course entity to CourseDto with rating stats
    private CourseDto mapToDtoWithStats(Course course) {
        CourseDto courseDto = mapToDto(course);

        // Add rating statistics
        Double avgRating = reviewRepository.getAverageRatingForCourse(course);
        courseDto.setAverageRating(avgRating);

        Integer reviewCount = reviewRepository.findByCourse(course).size();
        courseDto.setReviewCount(reviewCount);

        return courseDto;
    }


    @Override
    public Course getCourseByExerciseId(Long exerciseId) {
        Exercise exercise = exerciseService.getExerciseEntityById(exerciseId);
        return exercise.getCourse();
    }

    @Override
    public Course getCourseByExamId(Long examId) {
        Exam exam = examService.getExamEntityById(examId);
        return exam.getCourse();
    }

}