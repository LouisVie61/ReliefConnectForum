package demo.reliefconnectforum.controller;

import demo.reliefconnectforum.dto.request.UserLoginRequest;
import demo.reliefconnectforum.dto.request.UserRegisterRequest;
import demo.reliefconnectforum.dto.response.UserLoginResponse;
import demo.reliefconnectforum.dto.response.UserRegisterResponse;
import demo.reliefconnectforum.service.auth.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and authorization operations")
@SecurityRequirement(name = "Bearer Authentication")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<UserLoginResponse> login(
            @Parameter(description = "Login credentials", required = true, example = "email: 23021693@vnu.edu.vn, password: Tan568993993@")
            @RequestBody UserLoginRequest request) {
        UserLoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<UserRegisterResponse> register(
            @Parameter(description = "User registration details", required = true)
            @RequestBody UserRegisterRequest request) {
        UserRegisterResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token", description = "Get new access token using refresh token")
    public ResponseEntity<UserLoginResponse> refreshToken(
            @Parameter(description = "Refresh token", required = true)
            @RequestParam String refreshToken) {
        UserLoginResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidate user session")
    public ResponseEntity<String> logout(@RequestParam String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok("Logged out successfully");
    }
}