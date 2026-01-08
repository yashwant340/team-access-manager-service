package com.example.accessManager.controller;

import com.example.accessManager.dto.AuthRequest;
import com.example.accessManager.dto.LoginUserDTO;
import com.example.accessManager.entity.User;
import com.example.accessManager.repository.UserRepository;
import com.example.accessManager.service.AuthService;
import com.example.accessManager.service.EmailService;
import com.example.accessManager.service.OtpService;
import com.example.accessManager.wrapper.LoginRequestWrapper;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

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

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) throws MessagingException, MessagingException {
        String username = request.get("username");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = otpService.generateOtp(username);
        emailService.sendOtpEmail(user.getEmail(), otp);

        return ResponseEntity.ok("OTP sent to registered email");
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String otp = request.get("otp");

        if (!otpService.validateOtp(username, otp)) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
        return ResponseEntity.ok("OTP verified");
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String newPassword = request.get("newPassword");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Password updated successfully");
    }





}

