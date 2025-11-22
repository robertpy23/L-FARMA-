// Crear: ./src/main/java/com/App/Lfarma/controller/AuthRestController.java
package com.App.Lfarma.controller;

import com.App.Lfarma.DTO.LoginRequest;
import com.App.Lfarma.DTO.JwtResponse;
import com.App.Lfarma.DTO.ApiResponse;
import com.App.Lfarma.security.JwtUtil;
import com.App.Lfarma.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:53589"})
public class AuthRestController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Autenticar usuario
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Obtener rol del usuario
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_USER")
                    .replace("ROLE_", "");

            // Generar token JWT
            String jwt = jwtUtil.generateToken(loginRequest.getUsername(), role);

            return ResponseEntity.ok(new JwtResponse(
                jwt, 
                loginRequest.getUsername(), 
                role, 
                "Login exitoso"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Credenciales inválidas: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                .body(new ApiResponse(false, "Usuario no autenticado"));
        }

        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER")
                .replace("ROLE_", "");

        return ResponseEntity.ok(new ApiResponse(true, "Usuario actual", 
            new JwtResponse(null, username, role, "Usuario autenticado")));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Token no proporcionado"));
        }

        String token = authHeader.substring(7);
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);
            return ResponseEntity.ok(new JwtResponse(token, username, role, "Token válido"));
        } else {
            return ResponseEntity.status(401)
                .body(new ApiResponse(false, "Token inválido o expirado"));
        }
    }
}