package com.nihongo.learningplatform.mapper;

import com.nihongo.learningplatform.dto.MockExamAttemptDto;
import com.nihongo.learningplatform.entity.MockExamAttempt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Arrays;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MockExamAttemptMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "mockExamId", source = "mockExam.id")
    @Mapping(target = "mockExamTitle", source = "mockExam.title")
    @Mapping(target = "answers", source = "answers", qualifiedByName = "stringToMap")
    MockExamAttemptDto toDto(MockExamAttempt attempt);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "mockExam", ignore = true)
    @Mapping(target = "answers", source = "answers", qualifiedByName = "mapToString")
    MockExamAttempt toEntity(MockExamAttemptDto dto);

    @Named("stringToMap")
    default Map<Long, List<Long>> stringToMap(String answers) {
        if (answers == null || answers.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<Long>> result = new HashMap<>();
        try {
            // Parse string representation of map like "{1=[2, 3], 4=[5]}"
            String content = answers.substring(1, answers.length() - 1); // Remove outer curly braces
            String[] entries = content.split(", (?=[0-9]+=)");

            for (String entry : entries) {
                String[] parts = entry.split("=");
                if (parts.length == 2) {
                    Long questionId = Long.parseLong(parts[0].trim());

                    // Parse the list part [1, 2, 3]
                    String listStr = parts[1].trim();
                    if (listStr.startsWith("[") && listStr.endsWith("]")) {
                        listStr = listStr.substring(1, listStr.length() - 1);
                        List<Long> answerIds = Arrays.stream(listStr.split(", "))
                                .filter(s -> !s.isEmpty())
                                .map(s -> Long.parseLong(s.trim()))
                                .collect(Collectors.toList());

                        result.put(questionId, answerIds);
                    }
                }
            }
        } catch (Exception e) {
            // In case of parsing error, return empty map
            return Collections.emptyMap();
        }

        return result;
    }

    @Named("mapToString")
    default String mapToString(Map<Long, List<Long>> answers) {
        if (answers == null) {
            return null;
        }
        return answers.toString();
    }
}