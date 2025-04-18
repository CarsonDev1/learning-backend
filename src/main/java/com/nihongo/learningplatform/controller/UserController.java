package com.nihongo.learningplatform.controller;

import com.nihongo.learningplatform.dto.ApiResponseDto;
import com.nihongo.learningplatform.dto.UserDto;
import com.nihongo.learningplatform.entity.UserRole;
import com.nihongo.learningplatform.service.UserService;
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
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/profile")
    public ResponseEntity<ApiResponseDto> getCurrentUser(Principal principal) {
        UserDto userDto = userService.getUserByUsername(principal.getName());

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "User profile retrieved successfully",
                userDto,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/user/profile")
    public ResponseEntity<ApiResponseDto> updateCurrentUser(@Valid @RequestBody UserDto userDto, Principal principal) {
        UserDto currentUser = userService.getUserByUsername(principal.getName());
        userDto.setId(currentUser.getId());

        UserDto updatedUser = userService.updateUser(currentUser.getId(), userDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "User profile updated successfully",
                updatedUser,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Users retrieved successfully",
                users,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "User retrieved successfully",
                user,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/users/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> getUsersByRole(@PathVariable UserRole role) {
        List<UserDto> users = userService.getUsersByRole(role);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "Users with role " + role + " retrieved successfully",
                users,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "User updated successfully",
                updatedUser,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/users/{id}/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> changeUserRole(@PathVariable Long id, @PathVariable UserRole role) {
        UserDto updatedUser = userService.changeUserRole(id, role);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "User role changed to " + role + " successfully",
                updatedUser,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/users/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> blockUser(@PathVariable Long id) {
        UserDto blockedUser = userService.blockUser(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "User blocked successfully",
                blockedUser,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/users/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> unblockUser(@PathVariable Long id) {
        UserDto unblockedUser = userService.unblockUser(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "User unblocked successfully",
                unblockedUser,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        ApiResponseDto apiResponse = new ApiResponseDto(
                true,
                "User deleted successfully",
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}