package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.ModuleDto;
import com.nihongo.learningplatform.entity.Module;

import java.util.List;

public interface ModuleService {
    ModuleDto createModule(ModuleDto moduleDto);
    ModuleDto getModuleById(Long id);
    List<ModuleDto> getModulesByCourse(Long courseId);
    ModuleDto updateModule(Long id, ModuleDto moduleDto);
    void deleteModule(Long id);
    Module getModuleEntityById(Long id);
}