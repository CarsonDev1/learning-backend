package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.ExamDto;
import com.nihongo.learningplatform.entity.Exam;

import java.util.List;

public interface ExamService {
    ExamDto createExam(ExamDto examDto);
    ExamDto getExamById(Long id);
    List<ExamDto> getExamsByCourse(Long courseId);
    List<ExamDto> getAiGeneratedExams();
    ExamDto updateExam(Long id, ExamDto examDto);
    void deleteExam(Long id);
    Exam getExamEntityById(Long id);
}