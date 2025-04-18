package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.ApiResponseDto;
import com.nihongo.learningplatform.dto.SolutionDto;
import com.nihongo.learningplatform.entity.Course;
import com.nihongo.learningplatform.entity.Question;
import com.nihongo.learningplatform.entity.SpeechExercise;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SolutionController {

    private final SolutionService solutionService;
    private final QuestionService questionService;
    private final SpeechExerciseService speechExerciseService;
    private final UserService userService;
    private final CourseService courseService;

    @Autowired
    public SolutionController(SolutionService solutionService,
                              QuestionService questionService,
                              SpeechExerciseService speechExerciseService,
                              UserService userService,
                              CourseService courseService) {
        this.solutionService = solutionService;
        this.questionService = questionService;
        this.speechExerciseService = speechExerciseService;
        this.userService = userService;
        this.courseService = courseService;
    }

    // Instructor endpoints for solution management

    @PostMapping("/instructor/solutions")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> createSolution(@Valid @RequestBody SolutionDto solutionDto, Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        // Verify if instructor has permission to add solution to this question/speech exercise
        Long courseId = null;

        if (solutionDto.getQuestionId() != null) {
            Question question = questionService.getQuestionEntityById(solutionDto.getQuestionId());

            if (question.getExercise() != null) {
                Course course = question.getExercise().getCourse();
                courseId = course.getId();

                // Check if instructor owns this course
                if (!isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
                    ApiResponseDto apiResponse = new ApiResponseDto(
                            false,
                            "You are not authorized to add solutions to questions in this course",
                            null,
                            LocalDateTime.now()
                    );

                    return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
                }
            } else if (question.getExam() != null) {
                Course course = question.getExam().getCourse();
                courseId = course.getId();

                // Check if instructor owns this course
                if (!isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
                    ApiResponseDto apiResponse = new ApiResponseDto(
                            false,
                            "You are not authorized to add solutions to questions in this course",
                            null,
                            LocalDateTime.now()
                    );

                    return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
                }
            }
        } else if (solutionDto.getSpeechExerciseId() != null) {
            SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(solutionDto.getSpeechExerciseId());
            Course course = speechExercise.getLesson().getCourse();
            courseId = course.getId();

            // Check if instructor owns this course
            if (!isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
                ApiResponseDto apiResponse = new ApiResponseDto(
                        false,
                        "You are not authorized to add solutions to speech exercises in this course",
                        null,
                        LocalDateTime.now()
                );

                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }
        } else {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "Solution must be linked to a question or speech exercise",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }

        SolutionDto createdSolution = solutionService.createSolution(solutionDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Solution created successfully",
                createdSolution,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/instructor/solutions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> getSolutionById(@PathVariable Long id, Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        SolutionDto solution = solutionService.getSolutionById(id);

        // Get the course associated with this solution
        Long courseId = null;
        Course course = null;

        if (solution.getQuestionId() != null) {
            Question question = questionService.getQuestionEntityById(solution.getQuestionId());

            if (question.getExercise() != null) {
                course = question.getExercise().getCourse();
            } else if (question.getExam() != null) {
                course = question.getExam().getCourse();
            }
        } else if (solution.getSpeechExerciseId() != null) {
            SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(solution.getSpeechExerciseId());
            course = speechExercise.getLesson().getCourse();
        }

        if (course != null) {
            courseId = course.getId();

            // Check if instructor owns this course
            if (!isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
                ApiResponseDto apiResponse = new ApiResponseDto(
                        false,
                        "You are not authorized to view solutions for this course",
                        null,
                        LocalDateTime.now()
                );

                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }
        }

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Solution retrieved successfully",
                solution,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/instructor/questions/{questionId}/solution")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> getSolutionByQuestion(@PathVariable Long questionId, Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        Question question = questionService.getQuestionEntityById(questionId);
        Course course = null;

        if (question.getExercise() != null) {
            course = question.getExercise().getCourse();
        } else if (question.getExam() != null) {
            course = question.getExam().getCourse();
        }

        if (course != null) {
            // Check if instructor owns this course
            if (!isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
                ApiResponseDto apiResponse = new ApiResponseDto(
                        false,
                        "You are not authorized to view solutions for this course",
                        null,
                        LocalDateTime.now()
                );

                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }
        }

        try {
            SolutionDto solution = solutionService.getSolutionByQuestionId(questionId);

            ApiResponseDto apiResponse = new ApiResponseDto(
                    true,
                    "Solution retrieved successfully",
                    solution,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "No solution found for this question",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/instructor/speech-exercises/{speechExerciseId}/solution")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> getSolutionBySpeechExercise(@PathVariable Long speechExerciseId, Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(speechExerciseId);
        Course course = speechExercise.getLesson().getCourse();

        // Check if instructor owns this course
        if (!isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to view solutions for this course",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        try {
            SolutionDto solution = solutionService.getSolutionBySpeechExerciseId(speechExerciseId);

            ApiResponseDto apiResponse = new ApiResponseDto(
                    true,
                    "Solution retrieved successfully",
                    solution,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "No solution found for this speech exercise",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/instructor/exercises/{exerciseId}/solutions")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> getSolutionsByExercise(@PathVariable Long exerciseId, Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        // Check if instructor has permission to view solutions for this exercise
        Course course = courseService.getCourseByExerciseId(exerciseId);

        if (!isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to view solutions for this exercise",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        List<SolutionDto> solutions = solutionService.getSolutionsByExerciseId(exerciseId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Solutions retrieved successfully",
                solutions,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/instructor/exams/{examId}/solutions")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> getSolutionsByExam(@PathVariable Long examId, Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        // Check if instructor has permission to view solutions for this exam
        Course course = courseService.getCourseByExamId(examId);

        if (!isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to view solutions for this exam",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        List<SolutionDto> solutions = solutionService.getSolutionsByExamId(examId);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Solutions retrieved successfully",
                solutions,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/instructor/solutions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> updateSolution(@PathVariable Long id,
                                                         @Valid @RequestBody SolutionDto solutionDto,
                                                         Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        // Check if instructor owns the solution's course
        SolutionDto existingSolution = solutionService.getSolutionById(id);
        Course course = null;

        if (existingSolution.getQuestionId() != null) {
            Question question = questionService.getQuestionEntityById(existingSolution.getQuestionId());

            if (question.getExercise() != null) {
                course = question.getExercise().getCourse();
            } else if (question.getExam() != null) {
                course = question.getExam().getCourse();
            }
        } else if (existingSolution.getSpeechExerciseId() != null) {
            SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(existingSolution.getSpeechExerciseId());
            course = speechExercise.getLesson().getCourse();
        }

        if (course != null && !isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to update this solution",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        solutionDto.setId(id);
        SolutionDto updatedSolution = solutionService.updateSolution(id, solutionDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Solution updated successfully",
                updatedSolution,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/instructor/solutions/{id}/visibility/{visible}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> setSolutionVisibility(@PathVariable Long id,
                                                                @PathVariable boolean visible,
                                                                Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        // Check if instructor owns the solution's course
        SolutionDto existingSolution = solutionService.getSolutionById(id);
        Course course = null;

        if (existingSolution.getQuestionId() != null) {
            Question question = questionService.getQuestionEntityById(existingSolution.getQuestionId());

            if (question.getExercise() != null) {
                course = question.getExercise().getCourse();
            } else if (question.getExam() != null) {
                course = question.getExam().getCourse();
            }
        } else if (existingSolution.getSpeechExerciseId() != null) {
            SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(existingSolution.getSpeechExerciseId());
            course = speechExercise.getLesson().getCourse();
        }

        if (course != null && !isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to update this solution",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        SolutionDto updatedSolution = visible ?
                solutionService.showSolution(id) :
                solutionService.hideSolution(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                visible ? "Solution is now visible" : "Solution is now hidden",
                updatedSolution,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/instructor/solutions/{id}/availability/{available}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> setSolutionAvailability(@PathVariable Long id,
                                                                  @PathVariable boolean available,
                                                                  Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        // Check if instructor owns the solution's course
        SolutionDto existingSolution = solutionService.getSolutionById(id);
        Course course = null;

        if (existingSolution.getQuestionId() != null) {
            Question question = questionService.getQuestionEntityById(existingSolution.getQuestionId());

            if (question.getExercise() != null) {
                course = question.getExercise().getCourse();
            } else if (question.getExam() != null) {
                course = question.getExam().getCourse();
            }
        } else if (existingSolution.getSpeechExerciseId() != null) {
            SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(existingSolution.getSpeechExerciseId());
            course = speechExercise.getLesson().getCourse();
        }

        if (course != null && !isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to update this solution",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        SolutionDto updatedSolution = solutionService.setAvailableAfterSubmission(id, available);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                available ? "Solution will be available after submission" : "Solution will be available to all enrolled users",
                updatedSolution,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/instructor/solutions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponseDto> deleteSolution(@PathVariable Long id, Principal principal) {
        User instructor = userService.getUserEntityByUsername(principal.getName());
        boolean isAdmin = instructor.getRole().name().equals("ADMIN");

        // Check if instructor owns the solution's course
        SolutionDto existingSolution = solutionService.getSolutionById(id);
        Course course = null;

        if (existingSolution.getQuestionId() != null) {
            Question question = questionService.getQuestionEntityById(existingSolution.getQuestionId());

            if (question.getExercise() != null) {
                course = question.getExercise().getCourse();
            } else if (question.getExam() != null) {
                course = question.getExam().getCourse();
            }
        } else if (existingSolution.getSpeechExerciseId() != null) {
            SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(existingSolution.getSpeechExerciseId());
            course = speechExercise.getLesson().getCourse();
        }

        if (course != null && !isAdmin && !course.getInstructor().getId().equals(instructor.getId())) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "You are not authorized to delete this solution",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        }

        solutionService.deleteSolution(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Solution deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Student endpoints for accessing solutions

    @GetMapping("/student/questions/{questionId}/solution")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getStudentSolutionByQuestion(@PathVariable Long questionId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        try {
            // Find solution for the question
            SolutionDto solution = solutionService.getSolutionByQuestionId(questionId);

            // Find course ID for the question
            Question question = questionService.getQuestionEntityById(questionId);
            Long courseId = null;

            if (question.getExercise() != null) {
                courseId = question.getExercise().getCourse().getId();
            } else if (question.getExam() != null) {
                courseId = question.getExam().getCourse().getId();
            }

            if (courseId == null) {
                ApiResponseDto apiResponse = new ApiResponseDto(
                        false,
                        "Could not determine course for this question",
                        null,
                        LocalDateTime.now()
                );

                return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
            }

            // Check if user is allowed to view this solution
            boolean isAllowed = solutionService.isUserAllowedToViewSolution(solution.getId(), user.getId(), courseId);

            if (!isAllowed) {
                ApiResponseDto apiResponse = new ApiResponseDto(
                        false,
                        "You are not allowed to view this solution",
                        null,
                        LocalDateTime.now()
                );

                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }

            ApiResponseDto apiResponse = new ApiResponseDto(
                    true,
                    "Solution retrieved successfully",
                    solution,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "No solution found for this question",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/student/speech-exercises/{speechExerciseId}/solution")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<ApiResponseDto> getStudentSolutionBySpeechExercise(@PathVariable Long speechExerciseId, Principal principal) {
        User user = userService.getUserEntityByUsername(principal.getName());

        try {
            // Find solution for the speech exercise
            SolutionDto solution = solutionService.getSolutionBySpeechExerciseId(speechExerciseId);

            // Find course ID for the speech exercise
            SpeechExercise speechExercise = speechExerciseService.getSpeechExerciseEntityById(speechExerciseId);
            Long courseId = speechExercise.getLesson().getCourse().getId();

            // Check if user is allowed to view this solution
            boolean isAllowed = solutionService.isUserAllowedToViewSolution(solution.getId(), user.getId(), courseId);

            if (!isAllowed) {
                ApiResponseDto apiResponse = new ApiResponseDto(
                        false,
                        "You are not allowed to view this solution",
                        null,
                        LocalDateTime.now()
                );

                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }

            ApiResponseDto apiResponse = new ApiResponseDto(
                    true,
                    "Solution retrieved successfully",
                    solution,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponseDto apiResponse = new ApiResponseDto(
                    false,
                    "No solution found for this speech exercise",
                    null,
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }
}