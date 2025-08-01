package com.bkb.scanner.service;

import com.bkb.scanner.dto.UserCreationRequest;
import com.bkb.scanner.dto.UserDto;
import com.bkb.scanner.entity.Role;
import com.bkb.scanner.entity.User;
import com.bkb.scanner.repository.RoleRepository;
import com.bkb.scanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEnabled(user.isEnabled());

        // --- NEW FIELDS MAPPED HERE ---
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setDepartment(user.getDepartment());
        // --- END NEW FIELDS ---

        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto createUser(UserCreationRequest request) {
        User newUser = new User();
        newUser.setUserId(request.getUserId());
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEnabled(request.isEnabled());

        // --- NEW FIELDS MAPPED HERE ---
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setRole(request.getRole());
        newUser.setDepartment(request.getDepartment());
        // --- END NEW FIELDS ---

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findByNameIn(request.getRoles()));
            newUser.setRoles(roles);
        }

        User savedUser = userRepository.save(newUser);
        return convertToDto(savedUser);
    }

    @Transactional
    public UserDto updateUserStatus(String userId, boolean isEnabled) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(isEnabled);
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }
}