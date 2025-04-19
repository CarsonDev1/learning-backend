package com.nihongo.learningplatform.mapper;

import com.nihongo.learningplatform.dto.MockExamAttemptDto;
import com.nihongo.learningplatform.dto.MockExamDto;
import com.nihongo.learningplatform.entity.MockExam;
import com.nihongo.learningplatform.entity.MockExamAttempt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {QuestionMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MockExamMapper {

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseName", source = "course.title")
    MockExamDto toDto(MockExam mockExam);

    @Mapping(target = "course", ignore = true)
    MockExam toEntity(MockExamDto dto);

    void updateEntityFromDto(MockExamDto dto, @MappingTarget MockExam entity);
}

