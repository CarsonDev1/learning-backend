package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.ExerciseDto;
import com.nihongo.learningplatform.dto.QuestionDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Exercise;
import com.nihongo.learningplatform.entity.Question;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.ExerciseRepository;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.ExerciseService;
import com.nihongo.learningplatform.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final CourseService courseService;
    private final QuestionService questionService;

    @Autowired
    public ExerciseServiceImpl(ExerciseRepository exerciseRepository,
                               CourseService courseService,
                               QuestionService questionService) {
        this.exerciseRepository = exerciseRepository;
        this.courseService = courseService;
        this.questionService = questionService;
    }

    @Override
    @Transactional
    public ExerciseDto createExercise(ExerciseDto exerciseDto) {
        Course course = courseService.getCourseEntityById(exerciseDto.getCourseId());

        Exercise exercise = new Exercise();
        exercise.setTitle(exerciseDto.getTitle());
        exercise.setDescription(exerciseDto.getDescription());
        exercise.setAiGenerated(exerciseDto.isAiGenerated());
        exercise.setCourse(course);

        Exercise savedExercise = exerciseRepository.save(exercise);

        // Create questions for the exercise if any
        if (exerciseDto.getQuestions() != null && !exerciseDto.getQuestions().isEmpty()) {
            List<Question> questions = new ArrayList<>();
            for (QuestionDto questionDto : exerciseDto.getQuestions()) {
                questionDto.setExerciseId(savedExercise.getId());
                Question question = questionService.createQuestionForExercise(questionDto);
                questions.add(question);
            }
        }

        return mapToDto(savedExercise);
    }

    @Override
    public ExerciseDto getExerciseById(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with id: " + id));

        ExerciseDto exerciseDto = mapToDto(exercise);

        // Load questions for this exercise
        List<QuestionDto> questions = questionService.getQuestionsByExercise(id);
        exerciseDto.setQuestions(questions);

        return exerciseDto;
    }

    @Override
    public List<ExerciseDto> getExercisesByCourse(Long courseId) {
        Course course = courseService.getCourseEntityById(courseId);
        List<Exercise> exercises = exerciseRepository.findByCourse(course);
        return exercises.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExerciseDto> getAiGeneratedExercises() {
        List<Exercise> exercises = exerciseRepository.findByIsAiGenerated(true);
        return exercises.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExerciseDto updateExercise(Long id, ExerciseDto exerciseDto) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with id: " + id));

        exercise.setTitle(exerciseDto.getTitle());
        exercise.setDescription(exerciseDto.getDescription());

        Exercise updatedExercise = exerciseRepository.save(exercise);

        // Update questions if provided
        if (exerciseDto.getQuestions() != null && !exerciseDto.getQuestions().isEmpty()) {
            for (QuestionDto questionDto : exerciseDto.getQuestions()) {
                if (questionDto.getId() != null) {
                    questionService.updateQuestion(questionDto.getId(), questionDto);
                } else {
                    questionDto.setExerciseId(id);
                    questionService.createQuestionForExercise(questionDto);
                }
            }
        }

        return getExerciseById(id); // Return with updated questions
    }

    @Override
    @Transactional
    public void deleteExercise(Long id) {
        if (!exerciseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exercise not found with id: " + id);
        }
        exerciseRepository.deleteById(id);
    }

    @Override
    public Exercise getExerciseEntityById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with id: " + id));
    }

    // Helper method to map Exercise entity to ExerciseDto
    private ExerciseDto mapToDto(Exercise exercise) {
        ExerciseDto exerciseDto = new ExerciseDto();
        exerciseDto.setId(exercise.getId());
        exerciseDto.setTitle(exercise.getTitle());
        exerciseDto.setDescription(exercise.getDescription());
        exerciseDto.setAiGenerated(exercise.isAiGenerated());
        exerciseDto.setCourseId(exercise.getCourse().getId());
        return exerciseDto;
    }
}