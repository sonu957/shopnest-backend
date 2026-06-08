package com.shopnest.controller;

import com.shopnest.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> result = authService.register(
            request.getName(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("success", true, "message", "Registered successfully", "data", result));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> result = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("success", true, "message", "Login successful", "data", result));
    }

    @Data
    static class RegisterRequest {
        @NotBlank private String name;
        @Email @NotBlank private String email;
        @NotBlank @Size(min = 6) private String password;
    }

    @Data
    static class LoginRequest {
        @Email @NotBlank private String email;
        @NotBlank private String password;
    }
}
