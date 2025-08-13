package com.example.accessManager.controller;

import com.example.accessManager.dto.AuthRequest;
import com.example.accessManager.dto.AuthResponse;
import com.example.accessManager.dto.LoginUserDTO;
import com.example.accessManager.entity.User;
import com.example.accessManager.mapper.UserMapper;
import com.example.accessManager.repository.UserRepository;
import com.example.accessManager.service.AuthService;
import com.example.accessManager.utils.JwtUtility;
import com.example.accessManager.wrapper.LoginRequestWrapper;
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

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        return authService.handleLogin(authRequest);
    }

    @GetMapping("/me")
    public ResponseEntity<LoginUserDTO> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return authService.getMeDetails(userDetails);
    }

    @PostMapping("/login-request")
    public void saveAccessRequest(@RequestBody LoginRequestWrapper wrapper){
         authService.saveAccessRequest(wrapper);
    }


}

