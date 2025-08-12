package com.bkb.scanner.controller;

import com.bkb.scanner.dto.AuthRequest;
import com.bkb.scanner.dto.AuthResponse;
import com.bkb.scanner.entity.Role;
import com.bkb.scanner.entity.User;
import com.bkb.scanner.repository.UserRepository;
import com.bkb.scanner.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        // Get the full user details
        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get the first role (assuming single role per user, adjust if multiple roles)
        Role userRole = user.getRoles().isEmpty() ? null : user.getRoles().iterator().next();
        String roleName = userRole != null ? userRole.getName() : "";
        String roleLabel = userRole != null ? userRole.getLabel() : "";

        // Get all permissions
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        AuthResponse response = AuthResponse.builder()
                .jwt(jwt)
                .username(user.getUsername())
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(roleName)
                .roleLabel(roleLabel)
                .permissions(permissions)
                .expiresIn(jwtUtil.getExpirationTime() / 1000) // Convert to seconds
                .department(user.getDepartment())
                .build();

        return ResponseEntity.ok(response);
    }
}