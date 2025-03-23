package com.silveo.devices.controller;

import com.silveo.devices.entity.dto.AuthRequest;
import com.silveo.devices.entity.dto.AuthResponse;
import com.silveo.devices.service.CustomUserDetailsService;
import com.silveo.devices.service.util.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "Аутентификация")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Авторизация", description = "Вход пользователя под своей учетной записью")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        log.info("Trying to authorize...");
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверное имя пользователя или пароль");
        }
    }
}
