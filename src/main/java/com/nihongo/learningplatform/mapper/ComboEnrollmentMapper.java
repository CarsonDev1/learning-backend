package com.nihongo.learningplatform.mapper;

import com.nihongo.learningplatform.dto.ComboEnrollmentDto;
import com.nihongo.learningplatform.entity.ComboEnrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ComboEnrollmentMapper {

    ComboEnrollmentDto toDto(ComboEnrollment entity);

    ComboEnrollment toEntity(ComboEnrollmentDto dto);

    void updateEntityFromDto(ComboEnrollmentDto dto, @MappingTarget ComboEnrollment entity);
}