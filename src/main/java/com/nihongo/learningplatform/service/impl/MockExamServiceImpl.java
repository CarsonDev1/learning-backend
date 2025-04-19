package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.MockExamAttemptDto;
import com.nihongo.learningplatform.dto.MockExamDto;
import com.nihongo.learningplatform.entity.*;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.mapper.MockExamAttemptMapper;
import com.nihongo.learningplatform.mapper.MockExamMapper;
import com.nihongo.learningplatform.repository.CourseRepository;
import com.nihongo.learningplatform.repository.MockExamAttemptRepository;
import com.nihongo.learningplatform.repository.MockExamRepository;
import com.nihongo.learningplatform.repository.QuestionRepository;
import com.nihongo.learningplatform.repository.UserRepository;
import com.nihongo.learningplatform.service.MockExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MockExamServiceImpl implements MockExamService {

    @Autowired
    private MockExamRepository mockExamRepository;

    @Autowired
    private MockExamAttemptRepository mockExamAttemptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private MockExamMapper mockExamMapper;

    @Autowired
    private MockExamAttemptMapper mockExamAttemptMapper;

    @Override
    @Transactional
    public MockExamDto createMockExam(MockExamDto mockExamDto) {
        MockExam mockExam = mockExamMapper.toEntity(mockExamDto);

        // Set course if courseId is provided
        if (mockExamDto.getCourseId() != null) {
            Course course = courseRepository.findById(mockExamDto.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + mockExamDto.getCourseId()));
            mockExam.setCourse(course);
        }

        MockExam savedMockExam = mockExamRepository.save(mockExam);
        return mockExamMapper.toDto(savedMockExam);
    }

    @Override
    public MockExamDto getMockExamById(Long id) {
        MockExam mockExam = getMockExamEntityById(id);
        return mockExamMapper.toDto(mockExam);
    }

    @Override
    public List<MockExamDto> getAllMockExams() {
        return mockExamRepository.findAll().stream()
                .map(mockExamMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MockExamDto> getMockExamsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        return mockExamRepository.findByCourse(course).stream()
                .map(mockExamMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MockExamDto> getMockExamsByLevel(String level) {
        return mockExamRepository.findByLevel(level).stream()
                .map(mockExamMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MockExamDto> getGeneralMockExams() {
        return mockExamRepository.findByCourseIsNull().stream()
                .map(mockExamMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MockExamDto updateMockExam(Long id, MockExamDto mockExamDto) {
        MockExam existingMockExam = getMockExamEntityById(id);

        // Update basic fields
        existingMockExam.setTitle(mockExamDto.getTitle());
        existingMockExam.setDescription(mockExamDto.getDescription());
        existingMockExam.setTimeLimit(mockExamDto.getTimeLimit());
        existingMockExam.setPassingScore(mockExamDto.getPassingScore());
        existingMockExam.setLevel(mockExamDto.getLevel());
        existingMockExam.setAiGenerated(mockExamDto.isAiGenerated());

        // Update course if provided
        if (mockExamDto.getCourseId() != null) {
            Course course = courseRepository.findById(mockExamDto.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + mockExamDto.getCourseId()));
            existingMockExam.setCourse(course);
        } else {
            existingMockExam.setCourse(null);
        }

        // Save the updated mock exam
        MockExam updatedMockExam = mockExamRepository.save(existingMockExam);
        return mockExamMapper.toDto(updatedMockExam);
    }

    @Override
    @Transactional
    public void deleteMockExam(Long id) {
        if (!mockExamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mock exam not found with id: " + id);
        }
        mockExamRepository.deleteById(id);
    }

    @Override
    public MockExam getMockExamEntityById(Long id) {
        return mockExamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mock exam not found with id: " + id));
    }

    // Mock exam attempts implementation

    @Override
    @Transactional
    public MockExamAttemptDto startMockExamAttempt(Long mockExamId, Long userId) {
        MockExam mockExam = getMockExamEntityById(mockExamId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        MockExamAttempt attempt = new MockExamAttempt();
        attempt.setMockExam(mockExam);
        attempt.setUser(user);
        // startTime will be set by @PrePersist

        MockExamAttempt savedAttempt = mockExamAttemptRepository.save(attempt);
        return mockExamAttemptMapper.toDto(savedAttempt);
    }

    @Override
    public MockExamAttemptDto getMockExamAttemptById(Long id) {
        MockExamAttempt attempt = mockExamAttemptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mock exam attempt not found with id: " + id));
        return mockExamAttemptMapper.toDto(attempt);
    }

    @Override
    public List<MockExamAttemptDto> getMockExamAttemptsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return mockExamAttemptRepository.findByUser(user).stream()
                .map(mockExamAttemptMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MockExamAttemptDto> getMockExamAttemptsByMockExam(Long mockExamId) {
        MockExam mockExam = getMockExamEntityById(mockExamId);

        return mockExamAttemptRepository.findByMockExam(mockExam).stream()
                .map(mockExamAttemptMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MockExamAttemptDto> getMockExamAttemptsByUserAndMockExam(Long userId, Long mockExamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        MockExam mockExam = getMockExamEntityById(mockExamId);

        return mockExamAttemptRepository.findByUserAndMockExam(user, mockExam).stream()
                .map(mockExamAttemptMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MockExamAttemptDto submitMockExamAttempt(Long id, Map<Long, List<Long>> answers) {
        MockExamAttempt attempt = mockExamAttemptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mock exam attempt not found with id: " + id));

        // Verify that the attempt is not already completed
        if (attempt.getEndTime() != null) {
            throw new IllegalStateException("This mock exam attempt has already been submitted");
        }

        // Calculate the score
        int totalQuestions = attempt.getMockExam().getQuestions().size();
        int correctAnswers = calculateCorrectAnswers(attempt.getMockExam().getQuestions(), answers);
        int score = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;

        // Update the attempt
        attempt.setEndTime(LocalDateTime.now());
        attempt.setAnswers(answers.toString()); // Store answers as a string representation
        attempt.setScore(score);
        attempt.setPassed(score >= attempt.getMockExam().getPassingScore());

        MockExamAttempt savedAttempt = mockExamAttemptRepository.save(attempt);
        return mockExamAttemptMapper.toDto(savedAttempt);
    }

    @Override
    public Integer getHighestScoreByUserAndMockExam(Long userId, Long mockExamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        MockExam mockExam = getMockExamEntityById(mockExamId);

        Integer highestScore = mockExamAttemptRepository.findHighestScoreByUserAndMockExam(user, mockExam);
        return highestScore != null ? highestScore : 0;
    }

    // Helper method to calculate correct answers
    private int calculateCorrectAnswers(Set<Question> questions, Map<Long, List<Long>> userAnswers) {
        int correctCount = 0;

        for (Question question : questions) {
            Long questionId = question.getId();
            List<Long> selectedAnswerIds = userAnswers.getOrDefault(questionId, Collections.emptyList());

            // Get the correct answer IDs for this question
            List<Long> correctAnswerIds = question.getAnswers().stream()
                    .filter(Answer::isCorrect)
                    .map(Answer::getId)
                    .toList();

            // Check if the selected answers match the correct answers exactly
            if (selectedAnswerIds.size() == correctAnswerIds.size() &&
                    new HashSet<>(selectedAnswerIds).containsAll(correctAnswerIds)) {
                correctCount++;
            }
        }

        return correctCount;
    }
}