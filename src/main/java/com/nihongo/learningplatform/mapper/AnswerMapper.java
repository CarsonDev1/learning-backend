package com.nihongo.learningplatform.mapper;

import com.nihongo.learningplatform.dto.AnswerDto;
import com.nihongo.learningplatform.entity.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AnswerMapper {

    @Mapping(target = "questionId", source = "question.id")
    AnswerDto toDto(Answer answer);

    @Mapping(target = "question", ignore = true)
    Answer toEntity(AnswerDto dto);

    void updateEntityFromDto(AnswerDto dto, @MappingTarget Answer entity);
}