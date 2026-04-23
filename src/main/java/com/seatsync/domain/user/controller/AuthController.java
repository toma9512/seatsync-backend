package com.seatsync.domain.user.controller;

import com.seatsync.domain.user.service.AuthService;
import com.seatsync.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<Void> signUp(@RequestBody SignUpRequest request) {
        authService.signUp(request.email(), request.password(), request.name());
        return ApiResponse.success("회원가입이 완료되었습니다.", null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ApiResponse.success(new LoginResponse(token));
    }

    public record SignUpRequest(String email, String password, String name) {}
    public record LoginRequest(String email, String password) {}
    public record LoginResponse(String token) {}
}