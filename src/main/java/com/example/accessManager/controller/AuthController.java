package com.example.accessManager.controller;

import com.example.accessManager.dto.AuthRequest;
import com.example.accessManager.dto.LoginUserDTO;
import com.example.accessManager.entity.User;
import com.example.accessManager.repository.UserRepository;
import com.example.accessManager.service.AuthService;
import com.example.accessManager.service.EmailService;
import com.example.accessManager.service.OtpService;
import com.example.accessManager.wrapper.LoginRequestWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        return sendPasswordResetOtp(request, false);
    }

    @PostMapping("/forgot-password/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> request) {
        return sendPasswordResetOtp(request, true);
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String username = getRequiredValue(request, "username");
        String otp = getRequiredValue(request, "otp");

        if (username == null || otp == null) {
            return badRequest("Username and OTP are required.");
        }

        OtpService.OtpVerificationResult result = otpService.verifyOtp(username, otp);
        return switch (result.status()) {
            case VERIFIED -> ResponseEntity.ok(Map.of(
                    "message", "OTP verified. You can now reset your password.",
                    "resetToken", result.resetToken(),
                    "resetTokenExpiresIn", result.resetTokenExpiresIn()
            ));
            case EXPIRED -> ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("message", "This OTP has expired. Request a new code."));
            case TOO_MANY_ATTEMPTS -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many incorrect OTP attempts. Request a new code."));
            case ALREADY_USED -> badRequest("This OTP was already used. Request a new code if you still need to reset your password.");
            case NOT_FOUND -> badRequest("No active OTP was found. Request a new code.");
            case INVALID -> badRequest("The OTP is incorrect. Please try again.");
        };
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String username = getRequiredValue(request, "username");
        String newPassword = request == null ? null : request.get("newPassword");
        String resetToken = getRequiredValue(request, "resetToken");

        if (username == null || newPassword == null || newPassword.isBlank() || resetToken == null) {
            return badRequest("Username, new password, and a verified reset token are required.");
        }

        User user = userRepository.findByUsername(username)
                .filter(existingUser -> Boolean.TRUE.equals(existingUser.getIsActive()))
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No active user account was found for that username."));
        }

        if (!otpService.consumePasswordResetToken(username, resetToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Your verified reset session is invalid or has expired. Start again with a new OTP."));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password updated successfully. You can now sign in."));
    }

    private ResponseEntity<?> sendPasswordResetOtp(Map<String, String> request, boolean isResend) {
        String username = getRequiredValue(request, "username");
        if (username == null) {
            return badRequest("Username is required.");
        }

        User user = userRepository.findByUsername(username)
                .filter(existingUser -> Boolean.TRUE.equals(existingUser.getIsActive()))
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No active user account was found for that username."));
        }

        OtpService.OtpIssueResult issueResult = otpService.issueOtp(username);
        if (!issueResult.issued()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "message", "Please wait " + issueResult.resendAvailableIn() + " seconds before requesting another OTP.",
                    "resendAvailableIn", issueResult.resendAvailableIn()
            ));
        }

        try {
            emailService.sendOtpEmail(user.getEmail(), issueResult.otp());
        } catch (Exception exception) {
            otpService.invalidateIssuedOtp(username, issueResult.otp());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "We could not send the OTP. Please try again."));
        }

        return ResponseEntity.ok(Map.of(
                "message", isResend ? "A new OTP has been sent to your registered email." : "OTP sent to your registered email.",
                "resendAvailableIn", issueResult.resendAvailableIn(),
                "otpExpiresIn", issueResult.otpExpiresIn()
        ));
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    private String getRequiredValue(Map<String, String> request, String fieldName) {
        if (request == null || request.get(fieldName) == null) {
            return null;
        }

        String value = request.get(fieldName).trim();
        return value.isEmpty() ? null : value;
    }
}
