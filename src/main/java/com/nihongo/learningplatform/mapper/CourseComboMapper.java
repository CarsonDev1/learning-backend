package com.nihongo.learningplatform.mapper;

import com.nihongo.learningplatform.dto.CourseComboDto;
import com.nihongo.learningplatform.entity.CourseCombo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CourseComboMapper {

    CourseComboDto toDto(CourseCombo entity);

    CourseCombo toEntity(CourseComboDto dto);

    void updateEntityFromDto(CourseComboDto dto, @MappingTarget CourseCombo entity);
}