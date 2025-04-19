package com.nihongo.learningplatform.mapper;

import com.nihongo.learningplatform.dto.QuestionDto;
import com.nihongo.learningplatform.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {AnswerMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QuestionMapper {

    @Mapping(target = "exerciseId", source = "exercise.id")
    @Mapping(target = "examId", source = "exam.id")
    QuestionDto toDto(Question question);

    @Mapping(target = "exercise", ignore = true)
    @Mapping(target = "exam", ignore = true)
    @Mapping(target = "mockExam", ignore = true)
    Question toEntity(QuestionDto dto);

    void updateEntityFromDto(QuestionDto dto, @MappingTarget Question entity);
}