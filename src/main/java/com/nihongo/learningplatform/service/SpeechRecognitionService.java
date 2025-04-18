package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.SpeechRecognitionRequestDto;

public interface SpeechRecognitionService {
    Float recognizeAndEvaluateSpeech(SpeechRecognitionRequestDto requestDto);
}