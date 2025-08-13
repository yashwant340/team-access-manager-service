package com.example.accessManager.service.impl;

import com.example.accessManager.dto.AuthRequest;
import com.example.accessManager.dto.AuthResponse;
import com.example.accessManager.dto.LoginUserDTO;
import com.example.accessManager.entity.LoginRequest;
import com.example.accessManager.entity.User;
import com.example.accessManager.mapper.UserMapper;
import com.example.accessManager.repository.LoginRequestRepository;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authManager;
    private final JwtUtility jwtUtil;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final LoginRequestRepository loginRequestRepository;

    @Override
    public ResponseEntity<?> handleLogin(AuthRequest authRequest) {
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

    @Override
    public ResponseEntity<LoginUserDTO> getMeDetails(UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(userMapper.userToLoginUserDto(user));
    }

    @Override
    public void saveAccessRequest(LoginRequestWrapper wrapper) {
        LoginRequest loginRequest = LoginRequest.builder()
                .name(wrapper.getName())
                .email(wrapper.getEmail())
                .empId(wrapper.getEmpId())
                .team(wrapper.getTeam())
                .role(wrapper.getRole())
                .createdDate(new Date())
                .updatedDate(new Date())
                .isActive(true)
                .build();

        loginRequestRepository.save(loginRequest);

    }
}
