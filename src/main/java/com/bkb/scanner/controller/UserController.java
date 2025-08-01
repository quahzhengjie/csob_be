package com.bkb.scanner.controller;

import com.bkb.scanner.dto.UserCreationRequest;
import com.bkb.scanner.dto.UserDto;
import com.bkb.scanner.dto.UserStatusUpdateRequest;
import com.bkb.scanner.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreationRequest request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('admin:manage-users')")
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable String userId, @Valid @RequestBody UserStatusUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserStatus(userId, request.isEnabled()));
    }
}