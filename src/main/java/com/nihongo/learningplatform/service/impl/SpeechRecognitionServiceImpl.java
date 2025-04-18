package com.nihongo.learningplatform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nihongo.learningplatform.dto.SpeechRecognitionRequestDto;
import com.nihongo.learningplatform.entity.SpeechExercise;
import com.nihongo.learningplatform.service.SpeechExerciseService;
import com.nihongo.learningplatform.service.SpeechRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SpeechRecognitionServiceImpl implements SpeechRecognitionService {

    private final RestTemplate restTemplate;
    private final SpeechExerciseService speechExerciseService;

    @Value("${speech.recognition.api.key}")
    private String apiKey;

    @Value("${speech.recognition.api.url}")
    private String apiUrl;

    @Autowired
    public SpeechRecognitionServiceImpl(RestTemplate restTemplate, SpeechExerciseService speechExerciseService) {
        this.restTemplate = restTemplate;
        this.speechExerciseService = speechExerciseService;
    }

    @Override
    public Float recognizeAndEvaluateSpeech(SpeechRecognitionRequestDto requestDto) {
        try {
            // Get target text from speech exercise
            SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(requestDto.getSpeechExerciseId());
            String targetText = speechExercise.getJapaneseText();

            // Prepare API request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("audio", requestDto.getAudioData());
            requestBody.put("language", "ja-JP");
            requestBody.put("targetText", targetText);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Call speech recognition API
            String response = restTemplate.postForObject(apiUrl, entity, String.class);

            // Parse response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // Extract recognized text and score
            String recognizedText = root.path("recognizedText").asText();
            Float pronunciationScore = root.path("pronunciationScore").floatValue();

            return pronunciationScore;

        } catch (Exception e) {
            throw new RuntimeException("Error processing speech recognition: " + e.getMessage(), e);
        }
    }
}