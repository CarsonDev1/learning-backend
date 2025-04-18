package com.nihongo.learningplatform.service.impl;

import com.nihongo.learningplatform.dto.SpeechExerciseDto;
import com.nihongo.learningplatform.entity.Lesson;
import com.nihongo.learningplatform.entity.SpeechExercise;
import com.nihongo.learningplatform.exception.ResourceNotFoundException;
import com.nihongo.learningplatform.repository.SpeechExerciseRepository;
import com.nihongo.learningplatform.service.LessonService;
import com.nihongo.learningplatform.service.SpeechExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpeechExerciseServiceImpl implements SpeechExerciseService {

    private final SpeechExerciseRepository speechExerciseRepository;
    private final LessonService lessonService;

    @Autowired
    public SpeechExerciseServiceImpl(SpeechExerciseRepository speechExerciseRepository,
                                     LessonService lessonService) {
        this.speechExerciseRepository = speechExerciseRepository;
        this.lessonService = lessonService;
    }

    @Override
    @Transactional
    public SpeechExerciseDto createSpeechExercise(SpeechExerciseDto speechExerciseDto) {
        Lesson lesson = lessonService.getLessonEntityById(speechExerciseDto.getLessonId());

        SpeechExercise speechExercise = new SpeechExercise();
        speechExercise.setTitle(speechExerciseDto.getTitle());
        speechExercise.setJapaneseText(speechExerciseDto.getJapaneseText());
        speechExercise.setAudioUrl(speechExerciseDto.getAudioUrl());
        speechExercise.setLesson(lesson);

        SpeechExercise savedSpeechExercise = speechExerciseRepository.save(speechExercise);
        return mapToDto(savedSpeechExercise);
    }

    @Override
    public SpeechExerciseDto getSpeechExerciseById(Long id) {
        SpeechExercise speechExercise = speechExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Speech exercise not found with id: " + id));
        return mapToDto(speechExercise);
    }

    @Override
    public List<SpeechExerciseDto> getSpeechExercisesByLesson(Long lessonId) {
        Lesson lesson = lessonService.getLessonEntityById(lessonId);
        List<SpeechExercise> speechExercises = speechExerciseRepository.findByLesson(lesson);
        return speechExercises.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SpeechExerciseDto updateSpeechExercise(Long id, SpeechExerciseDto speechExerciseDto) {
        SpeechExercise speechExercise = speechExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Speech exercise not found with id: " + id));

        speechExercise.setTitle(speechExerciseDto.getTitle());
        speechExercise.setJapaneseText(speechExerciseDto.getJapaneseText());
        speechExercise.setAudioUrl(speechExerciseDto.getAudioUrl());

        SpeechExercise updatedSpeechExercise = speechExerciseRepository.save(speechExercise);
        return mapToDto(updatedSpeechExercise);
    }

    @Override
    @Transactional
    public void deleteSpeechExercise(Long id) {
        if (!speechExerciseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Speech exercise not found with id: " + id);
        }
        speechExerciseRepository.deleteById(id);
    }

    @Override
    public SpeechExercise getSpeechExerciseEntityById(Long id) {
        return speechExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Speech exercise not found with id: " + id));
    }

    // Helper method to map SpeechExercise entity to SpeechExerciseDto
    private SpeechExerciseDto mapToDto(SpeechExercise speechExercise) {
        SpeechExerciseDto speechExerciseDto = new SpeechExerciseDto();
        speechExerciseDto.setId(speechExercise.getId());
        speechExerciseDto.setTitle(speechExercise.getTitle());
        speechExerciseDto.setJapaneseText(speechExercise.getJapaneseText());
        speechExerciseDto.setAudioUrl(speechExercise.getAudioUrl());
        speechExerciseDto.setLessonId(speechExercise.getLesson().getId());
        return speechExerciseDto;
    }
}