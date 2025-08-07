package com.example.accessManager.controller;

import com.example.accessManager.dto.AuthRequest;
import com.example.accessManager.dto.AuthResponse;
import com.example.accessManager.dto.LoginUserDTO;
import com.example.accessManager.entity.User;
import com.example.accessManager.mapper.UserMapper;
import com.example.accessManager.repository.UserRepository;
import com.example.accessManager.utils.JwtUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtility jwtUtil;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            System.out.println("Attempting login for: " + authRequest.getUsername());
            System.out.println(new BCryptPasswordEncoder().encode("admin123"));
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            System.out.println("Authenticated: " + auth.isAuthenticated());

            String token = jwtUtil.generateToken(authRequest.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (AuthenticationException ex) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Login failed: " + ex.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<LoginUserDTO> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(userMapper.userToLoginUserDto(user));
    }


}

