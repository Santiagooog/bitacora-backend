package com.sistema.bitacora.controller;

import com.sistema.bitacora.dto.auth.AuthResponse;
import com.sistema.bitacora.dto.auth.LoginRequest;
import com.sistema.bitacora.dto.auth.RegisterRequest; // ✅ Importar
import com.sistema.bitacora.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) { // ✅ RegisterRequest
        return ResponseEntity.ok(authService.register(request));
    }
}