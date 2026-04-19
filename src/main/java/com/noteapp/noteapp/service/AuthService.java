package com.noteapp.noteapp.service;

import com.noteapp.noteapp.dto.auth.AuthResponse;
import com.noteapp.noteapp.dto.auth.LoginRequest;
import com.noteapp.noteapp.dto.auth.SignupRequest;
import com.noteapp.noteapp.model.AppUser;
import com.noteapp.noteapp.repository.AppUserRepository;
import com.noteapp.noteapp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse signup(SignupRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (appUserRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        String tenantId = resolveTenantId();
        AppUser user = AppUser.builder()
                .email(email)
                .name(request.name().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .tenantId(tenantId)
                .build();

        AppUser savedUser = appUserRepository.save(user);
        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getTenantId());
        return new AuthResponse(token, savedUser.getTenantId(), savedUser.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getTenantId());
        return new AuthResponse(token, user.getTenantId(), user.getEmail());
    }

    private String resolveTenantId() {
        return "tenant-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
