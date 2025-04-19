package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.MockExamAttemptDto;
import com.nihongo.learningplatform.dto.MockExamDto;
import com.nihongo.learningplatform.entity.MockExam;

import java.util.List;
import java.util.Map;

public interface MockExamService {
    MockExamDto createMockExam(MockExamDto mockExamDto);
    MockExamDto getMockExamById(Long id);
    List<MockExamDto> getAllMockExams();
    List<MockExamDto> getMockExamsByCourse(Long courseId);
    List<MockExamDto> getMockExamsByLevel(String level);
    List<MockExamDto> getGeneralMockExams();
    MockExamDto updateMockExam(Long id, MockExamDto mockExamDto);
    void deleteMockExam(Long id);
    MockExam getMockExamEntityById(Long id);

    // Mock exam attempts
    MockExamAttemptDto startMockExamAttempt(Long mockExamId, Long userId);
    MockExamAttemptDto getMockExamAttemptById(Long id);
    List<MockExamAttemptDto> getMockExamAttemptsByUser(Long userId);
    List<MockExamAttemptDto> getMockExamAttemptsByMockExam(Long mockExamId);
    List<MockExamAttemptDto> getMockExamAttemptsByUserAndMockExam(Long userId, Long mockExamId);
    MockExamAttemptDto submitMockExamAttempt(Long id, Map<Long, List<Long>> answers);
    Integer getHighestScoreByUserAndMockExam(Long userId, Long mockExamId);
}