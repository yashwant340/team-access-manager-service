package com.example.accessManager.service;

import com.example.accessManager.dto.AuthRequest;
import com.example.accessManager.dto.LoginUserDTO;
import com.example.accessManager.wrapper.LoginRequestWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    ResponseEntity<?> handleLogin(AuthRequest authRequest);

    ResponseEntity<LoginUserDTO> getMeDetails(UserDetails userDetails);

    void saveAccessRequest(LoginRequestWrapper wrapper);
}
