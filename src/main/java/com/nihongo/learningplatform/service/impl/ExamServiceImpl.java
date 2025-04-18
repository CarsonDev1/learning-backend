package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.ExamDto;
import com.nihongo.learningplatform.dto.QuestionDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Exam;
import com.nihongo.learningplatform.entity.Question;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.ExamRepository;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.ExamService;
import com.nihongo.learningplatform.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final CourseService courseService;
    private final QuestionService questionService;

    @Autowired
    public ExamServiceImpl(ExamRepository examRepository,
                           @Lazy CourseService courseService,
                           QuestionService questionService) {
        this.examRepository = examRepository;
        this.courseService = courseService;
        this.questionService = questionService;
    }

    @Override
    @Transactional
    public ExamDto createExam(ExamDto examDto) {
        Course course = courseService.getCourseEntityById(examDto.getCourseId());

        Exam exam = new Exam();
        exam.setTitle(examDto.getTitle());
        exam.setDescription(examDto.getDescription());
        exam.setTimeLimit(examDto.getTimeLimit());
        exam.setPassingScore(examDto.getPassingScore());
        exam.setAiGenerated(examDto.isAiGenerated());
        exam.setCourse(course);

        Exam savedExam = examRepository.save(exam);

        // Create questions for the exam if any
        if (examDto.getQuestions() != null && !examDto.getQuestions().isEmpty()) {
            List<Question> questions = new ArrayList<>();
            for (QuestionDto questionDto : examDto.getQuestions()) {
                questionDto.setExamId(savedExam.getId());
                Question question = questionService.createQuestionForExam(questionDto);
                questions.add(question);
            }
        }

        return mapToDto(savedExam);
    }

    @Override
    public ExamDto getExamById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

        ExamDto examDto = mapToDto(exam);

        // Load questions for this exam
        List<QuestionDto> questions = questionService.getQuestionsByExam(id);
        examDto.setQuestions(questions);

        return examDto;
    }

    @Override
    public List<ExamDto> getExamsByCourse(Long courseId) {
        Course course = courseService.getCourseEntityById(courseId);
        List<Exam> exams = examRepository.findByCourse(course);
        return exams.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExamDto> getAiGeneratedExams() {
        List<Exam> exams = examRepository.findByAiGenerated(true);
        return exams.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExamDto updateExam(Long id, ExamDto examDto) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

        exam.setTitle(examDto.getTitle());
        exam.setDescription(examDto.getDescription());
        exam.setTimeLimit(examDto.getTimeLimit());
        exam.setPassingScore(examDto.getPassingScore());

        Exam updatedExam = examRepository.save(exam);

        // Update questions if provided
        if (examDto.getQuestions() != null && !examDto.getQuestions().isEmpty()) {
            for (QuestionDto questionDto : examDto.getQuestions()) {
                if (questionDto.getId() != null) {
                    questionService.updateQuestion(questionDto.getId(), questionDto);
                } else {
                    questionDto.setExamId(id);
                    questionService.createQuestionForExam(questionDto);
                }
            }
        }

        return getExamById(id); // Return with updated questions
    }

    @Override
    @Transactional
    public void deleteExam(Long id) {
        if (!examRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exam not found with id: " + id);
        }
        examRepository.deleteById(id);
    }

    @Override
    public Exam getExamEntityById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
    }

    // Helper method to map Exam entity to ExamDto
    private ExamDto mapToDto(Exam exam) {
        ExamDto examDto = new ExamDto();
        examDto.setId(exam.getId());
        examDto.setTitle(exam.getTitle());
        examDto.setDescription(exam.getDescription());
        examDto.setTimeLimit(exam.getTimeLimit());
        examDto.setPassingScore(exam.getPassingScore());
        examDto.setAiGenerated(exam.isAiGenerated());
        examDto.setCourseId(exam.getCourse().getId());
        return examDto;
    }
}