package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.CourseDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.User;

import java.util.List;

public interface CourseService {
    CourseDto createCourse(CourseDto courseDto, User instructor);
    CourseDto getCourseById(Long id);
    List<CourseDto> getAllCourses();
    List<CourseDto> getCoursesByInstructor(User instructor);
    List<CourseDto> getApprovedAndActiveCourses();
    List<CourseDto> getUnapprovedCourses();
    List<CourseDto> getCoursesByLevel(String level);
    List<CourseDto> searchCourses(String keyword);
    CourseDto updateCourse(Long id, CourseDto courseDto);
    CourseDto approveCourse(Long id);
    CourseDto rejectCourse(Long id);
    CourseDto activateCourse(Long id);
    CourseDto deactivateCourse(Long id);
    Course getCourseByExerciseId(Long exerciseId);
    Course getCourseByExamId(Long examId);
    void deleteCourse(Long id);
    Course getCourseEntityById(Long id);
    Double getAverageRating(Long courseId);
    int getReviewCount(Long courseId);
}