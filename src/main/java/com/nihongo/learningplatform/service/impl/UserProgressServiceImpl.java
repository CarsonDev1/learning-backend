package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.UserProgressDto;
import com.nihongo.learningplatform.entity.*;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.UserProgressRepository;
import com.nihongo.learningplatform.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserProgressServiceImpl implements UserProgressService {

    private final UserProgressRepository userProgressRepository;
    private final UserService userService;
    private final LessonService lessonService;
    private final ExerciseService exerciseService;
    private final ExamService examService;
    private final SpeechExerciseService speechExerciseService;

    @Autowired
    public UserProgressServiceImpl(UserProgressRepository userProgressRepository,
                                   UserService userService,
                                   LessonService lessonService,
                                   ExerciseService exerciseService,
                                   @Lazy ExamService examService,
                                   SpeechExerciseService speechExerciseService) {
        this.userProgressRepository = userProgressRepository;
        this.userService = userService;
        this.lessonService = lessonService;
        this.exerciseService = exerciseService;
        this.examService = examService;
        this.speechExerciseService = speechExerciseService;
    }

    @Override
    @Transactional
    public UserProgressDto createOrUpdateLessonProgress(Long userId, Long lessonId, boolean completed) {
        User user = userService.getUserEntityById(userId);
        Lesson lesson = lessonService.getLessonEntityById(lessonId);

        Optional<UserProgress> existingProgress = userProgressRepository.findByUserAndLesson(user, lesson);

        UserProgress userProgress;
        if (existingProgress.isPresent()) {
            userProgress = existingProgress.get();
            userProgress.setCompleted(completed);
        } else {
            userProgress = new UserProgress();
            userProgress.setUser(user);
            userProgress.setLesson(lesson);
            userProgress.setCompleted(completed);
        }

        UserProgress savedProgress = userProgressRepository.save(userProgress);
        return mapToDto(savedProgress);
    }

    @Override
    @Transactional
    public UserProgressDto createOrUpdateExerciseProgress(Long userId, Long exerciseId, Integer score) {
        User user = userService.getUserEntityById(userId);
        Exercise exercise = exerciseService.getExerciseEntityById(exerciseId);

        Optional<UserProgress> existingProgress = userProgressRepository.findByUserAndExercise(user, exercise);

        UserProgress userProgress;
        if (existingProgress.isPresent()) {
            userProgress = existingProgress.get();
            userProgress.setScore(score);
            userProgress.setCompleted(true);
        } else {
            userProgress = new UserProgress();
            userProgress.setUser(user);
            userProgress.setExercise(exercise);
            userProgress.setScore(score);
            userProgress.setCompleted(true);
        }

        UserProgress savedProgress = userProgressRepository.save(userProgress);
        return mapToDto(savedProgress);
    }

    @Override
    @Transactional
    public UserProgressDto createOrUpdateExamProgress(Long userId, Long examId, Integer score) {
        User user = userService.getUserEntityById(userId);
        Exam exam = examService.getExamEntityById(examId);

        Optional<UserProgress> existingProgress = userProgressRepository.findByUserAndExam(user, exam);

        UserProgress userProgress;
        if (existingProgress.isPresent()) {
            userProgress = existingProgress.get();
            userProgress.setScore(score);
            userProgress.setCompleted(true);
        } else {
            userProgress = new UserProgress();
            userProgress.setUser(user);
            userProgress.setExam(exam);
            userProgress.setScore(score);
            userProgress.setCompleted(true);
        }

        UserProgress savedProgress = userProgressRepository.save(userProgress);
        return mapToDto(savedProgress);
    }

    @Override
    @Transactional
    public UserProgressDto createOrUpdateSpeechExerciseProgress(Long userId, Long speechExerciseId, String userAudioUrl, Float pronunciationScore) {
        User user = userService.getUserEntityById(userId);
        SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(speechExerciseId);

        Optional<UserProgress> existingProgress = userProgressRepository.findByUserAndSpeechExercise(user, speechExercise);

        UserProgress userProgress;
        if (existingProgress.isPresent()) {
            userProgress = existingProgress.get();
            userProgress.setUserAudioUrl(userAudioUrl);
            userProgress.setPronunciationScore(pronunciationScore);
            userProgress.setCompleted(true);
        } else {
            userProgress = new UserProgress();
            userProgress.setUser(user);
            userProgress.setSpeechExercise(speechExercise);
            userProgress.setUserAudioUrl(userAudioUrl);
            userProgress.setPronunciationScore(pronunciationScore);
            userProgress.setCompleted(true);
        }

        UserProgress savedProgress = userProgressRepository.save(userProgress);
        return mapToDto(savedProgress);
    }

    @Override
    public UserProgressDto getUserProgressById(Long id) {
        UserProgress userProgress = userProgressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User progress not found with id: " + id));
        return mapToDto(userProgress);
    }

    @Override
    public List<UserProgressDto> getUserProgressByUser(Long userId) {
        User user = userService.getUserEntityById(userId);
        List<UserProgress> progressList = userProgressRepository.findByUser(user);
        return progressList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserProgressDto getUserProgressByUserAndLesson(Long userId, Long lessonId) {
        User user = userService.getUserEntityById(userId);
        Lesson lesson = lessonService.getLessonEntityById(lessonId);

        UserProgress userProgress = userProgressRepository.findByUserAndLesson(user, lesson)
                .orElseThrow(() -> new ResourceNotFoundException("User progress not found for user id: " +
                        userId + " and lesson id: " + lessonId));

        return mapToDto(userProgress);
    }

    @Override
    public UserProgressDto getUserProgressByUserAndExercise(Long userId, Long exerciseId) {
        User user = userService.getUserEntityById(userId);
        Exercise exercise = exerciseService.getExerciseEntityById(exerciseId);

        UserProgress userProgress = userProgressRepository.findByUserAndExercise(user, exercise)
                .orElseThrow(() -> new ResourceNotFoundException("User progress not found for user id: " +
                        userId + " and exercise id: " + exerciseId));

        return mapToDto(userProgress);
    }

    @Override
    public UserProgressDto getUserProgressByUserAndExam(Long userId, Long examId) {
        User user = userService.getUserEntityById(userId);
        Exam exam = examService.getExamEntityById(examId);

        UserProgress userProgress = userProgressRepository.findByUserAndExam(user, exam)
                .orElseThrow(() -> new ResourceNotFoundException("User progress not found for user id: " +
                        userId + " and exam id: " + examId));

        return mapToDto(userProgress);
    }

    @Override
    public UserProgressDto getUserProgressByUserAndSpeechExercise(Long userId, Long speechExerciseId) {
        User user = userService.getUserEntityById(userId);
        SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(speechExerciseId);

        UserProgress userProgress = userProgressRepository.findByUserAndSpeechExercise(user, speechExercise)
                .orElseThrow(() -> new ResourceNotFoundException("User progress not found for user id: " +
                        userId + " and speech exercise id: " + speechExerciseId));

        return mapToDto(userProgress);
    }

    @Override
    public List<UserProgressDto> getCompletedProgressByUser(Long userId) {
        User user = userService.getUserEntityById(userId);
        List<UserProgress> progressList = userProgressRepository.findByUserAndCompleted(user, true);
        return progressList.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUserProgress(Long id) {
        if (!userProgressRepository.existsById(id)) {
            throw new ResourceNotFoundException("User progress not found with id: " + id);
        }
        userProgressRepository.deleteById(id);
    }

    // Helper method to map UserProgress entity to UserProgressDto
    private UserProgressDto mapToDto(UserProgress userProgress) {
        UserProgressDto progressDto = new UserProgressDto();
        progressDto.setId(userProgress.getId());
        progressDto.setUserId(userProgress.getUser().getId());
        progressDto.setCompleted(userProgress.isCompleted());
        progressDto.setScore(userProgress.getScore());
        progressDto.setUserAudioUrl(userProgress.getUserAudioUrl());
        progressDto.setPronunciationScore(userProgress.getPronunciationScore());

        if (userProgress.getLesson() != null) {
            progressDto.setLessonId(userProgress.getLesson().getId());
        }

        if (userProgress.getExercise() != null) {
            progressDto.setExerciseId(userProgress.getExercise().getId());
        }

        if (userProgress.getExam() != null) {
            progressDto.setExamId(userProgress.getExam().getId());
        }

        if (userProgress.getSpeechExercise() != null) {
            progressDto.setSpeechExerciseId(userProgress.getSpeechExercise().getId());
        }

        return progressDto;
    }
}