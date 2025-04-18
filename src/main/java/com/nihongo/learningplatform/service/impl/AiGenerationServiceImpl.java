package com.nihongo.learningplatform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nihongo.learningplatform.dto.*;
import com.nihongo.learningplatform.entity.QuestionType;
import com.nihongo.learningplatform.service.AiGenerationService;
import com.nihongo.learningplatform.service.CourseService;
import com.nihongo.learningplatform.service.ExamService;
import com.nihongo.learningplatform.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiGenerationServiceImpl implements AiGenerationService {

    private final RestTemplate restTemplate;
    private final CourseService courseService;
    private final ExerciseService exerciseService;
    private final ExamService examService;

    @Value("${ai.generation.api.key}")
    private String apiKey;

    @Value("${ai.generation.api.url}")
    private String apiUrl;

    @Autowired
    public AiGenerationServiceImpl(RestTemplate restTemplate,
                                   CourseService courseService,
                                   ExerciseService exerciseService,
                                   ExamService examService) {
        this.restTemplate = restTemplate;
        this.courseService = courseService;
        this.exerciseService = exerciseService;
        this.examService = examService;
    }

    @Override
    public ExerciseDto generateExercise(AiGenerationRequestDto requestDto) {
        try {
            // Get course information
            CourseDto courseDto = courseService.getCourseById(requestDto.getCourseId());

            // Prepare API request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("topic", requestDto.getTopic());
            requestBody.put("difficultyLevel", requestDto.getDifficultyLevel());
            requestBody.put("numberOfQuestions", requestDto.getNumberOfQuestions());
            requestBody.put("questionTypes", getQuestionTypeNames(requestDto.getQuestionTypes()));
            requestBody.put("isExam", false);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Call AI generation API
            String response = restTemplate.postForObject(apiUrl, entity, String.class);

            // Parse response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // Create exercise with AI-generated questions
            ExerciseDto exerciseDto = new ExerciseDto();
            exerciseDto.setTitle(root.path("title").asText());
            exerciseDto.setDescription(root.path("description").asText());
            exerciseDto.setCourseId(requestDto.getCourseId());
            exerciseDto.setAiGenerated(true);

            // Parse questions
            List<QuestionDto> questions = new ArrayList<>();
            JsonNode questionsNode = root.path("questions");

            for (JsonNode questionNode : questionsNode) {
                QuestionDto questionDto = new QuestionDto();
                questionDto.setContent(questionNode.path("content").asText());
                questionDto.setType(QuestionType.valueOf(questionNode.path("type").asText()));

                // For listening questions, handle audio URL
                if (questionDto.getType() == QuestionType.LISTENING) {
                    questionDto.setAudioUrl(questionNode.path("audioUrl").asText());
                }

                // Parse answers
                List<AnswerDto> answers = new ArrayList<>();
                JsonNode answersNode = questionNode.path("answers");

                for (JsonNode answerNode : answersNode) {
                    AnswerDto answerDto = new AnswerDto();
                    answerDto.setContent(answerNode.path("content").asText());
                    answerDto.setCorrect(answerNode.path("isCorrect").asBoolean());
                    answers.add(answerDto);
                }

                questionDto.setAnswers(answers);
                questions.add(questionDto);
            }

            exerciseDto.setQuestions(questions);

            // Create exercise in the database
            return exerciseService.createExercise(exerciseDto);

        } catch (Exception e) {
            throw new RuntimeException("Error generating exercise: " + e.getMessage(), e);
        }
    }

    @Override
    public ExamDto generateExam(AiGenerationRequestDto requestDto) {
        try {
            // Get course information
            CourseDto courseDto = courseService.getCourseById(requestDto.getCourseId());

            // Prepare API request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("topic", requestDto.getTopic());
            requestBody.put("difficultyLevel", requestDto.getDifficultyLevel());
            requestBody.put("numberOfQuestions", requestDto.getNumberOfQuestions());
            requestBody.put("questionTypes", getQuestionTypeNames(requestDto.getQuestionTypes()));
            requestBody.put("isExam", true);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Call AI generation API
            String response = restTemplate.postForObject(apiUrl, entity, String.class);

            // Parse response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // Create exam with AI-generated questions
            ExamDto examDto = new ExamDto();
            examDto.setTitle(root.path("title").asText());
            examDto.setDescription(root.path("description").asText());
            examDto.setCourseId(requestDto.getCourseId());
            examDto.setAiGenerated(true);
            examDto.setTimeLimit(60); // Default time limit of 60 minutes
            examDto.setPassingScore(70); // Default passing score of 70%

            // Parse questions
            List<QuestionDto> questions = new ArrayList<>();
            JsonNode questionsNode = root.path("questions");

            for (JsonNode questionNode : questionsNode) {
                QuestionDto questionDto = new QuestionDto();
                questionDto.setContent(questionNode.path("content").asText());
                questionDto.setType(QuestionType.valueOf(questionNode.path("type").asText()));

                // For listening questions, handle audio URL
                if (questionDto.getType() == QuestionType.LISTENING) {
                    questionDto.setAudioUrl(questionNode.path("audioUrl").asText());
                }

                // Parse answers
                List<AnswerDto> answers = new ArrayList<>();
                JsonNode answersNode = questionNode.path("answers");

                for (JsonNode answerNode : answersNode) {
                    AnswerDto answerDto = new AnswerDto();
                    answerDto.setContent(answerNode.path("content").asText());
                    answerDto.setCorrect(answerNode.path("isCorrect").asBoolean());
                    answers.add(answerDto);
                }

                questionDto.setAnswers(answers);
                questions.add(questionDto);
            }

            examDto.setQuestions(questions);

            // Create exam in the database
            return examService.createExam(examDto);

        } catch (Exception e) {
            throw new RuntimeException("Error generating exam: " + e.getMessage(), e);
        }
    }

    // Helper method to convert QuestionType enum to string list
    private List<String> getQuestionTypeNames(List<QuestionType> questionTypes) {
        List<String> typeNames = new ArrayList<>();
        for (QuestionType type : questionTypes) {
            typeNames.add(type.name());
        }
        return typeNames;
    }
}