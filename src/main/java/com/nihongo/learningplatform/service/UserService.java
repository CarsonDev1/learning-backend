package com.nihongo.learningplatform.service;

import com.nihongo.learningplatform.dto.UserDto;
import com.nihongo.learningplatform.dto.UserRegistrationDto;
import com.nihongo.learningplatform.entity.User;
import com.nihongo.learningplatform.entity.UserRole;

import java.util.List;

public interface UserService {
    UserDto registerUser(UserRegistrationDto registrationDto);
    UserDto getUserById(Long id);
    UserDto getUserByUsername(String username);
    List<UserDto> getAllUsers();
    List<UserDto> getUsersByRole(UserRole role);
    UserDto updateUser(Long id, UserDto userDto);
    UserDto changeUserRole(Long id, UserRole role);
    UserDto blockUser(Long id);
    UserDto unblockUser(Long id);
    void deleteUser(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User getUserEntityById(Long id);
    User getUserEntityByUsername(String username);
    void saveUser(User user);
}
