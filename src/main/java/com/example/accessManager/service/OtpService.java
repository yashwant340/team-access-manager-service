package com.example.accessManager.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 5;
    private static final int MAX_INVALID_ATTEMPTS = 5;

    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();
    private final Clock clock;
    private final SecureRandom secureRandom;

    public OtpService() {
        this(Clock.systemUTC(), new SecureRandom());
    }

    OtpService(Clock clock, SecureRandom secureRandom) {
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    /**
     * Creates a new OTP unless the previous send is still in its cooldown period.
     * Issuing a new code replaces every part of the previous reset session.
     */
    public synchronized OtpIssueResult issueOtp(String username) {
        Instant now = clock.instant();
        OtpData existingOtp = otpStore.get(username);

        if (existingOtp != null && existingOtp.resendAvailableAt().isAfter(now)) {
            return OtpIssueResult.cooldown(secondsRemaining(existingOtp.resendAvailableAt(), now));
        }

        String otp = String.format(Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000));
        otpStore.put(username, new OtpData(
                otp,
                now.plus(Duration.ofMinutes(OTP_EXPIRY_MINUTES)),
                now.plus(Duration.ofSeconds(RESEND_COOLDOWN_SECONDS)),
                null,
                null,
                0
        ));

        return OtpIssueResult.issued(otp, RESEND_COOLDOWN_SECONDS, OTP_EXPIRY_MINUTES * 60L);
    }

    /**
     * Verifies an OTP once and exchanges it for a short-lived password-reset token.
     */
    public synchronized OtpVerificationResult verifyOtp(String username, String otp) {
        Instant now = clock.instant();
        OtpData otpData = otpStore.get(username);

        if (otpData == null) {
            return OtpVerificationResult.of(OtpVerificationStatus.NOT_FOUND);
        }

        if (!otpData.otpExpiry().isAfter(now)) {
            otpStore.remove(username);
            return OtpVerificationResult.of(OtpVerificationStatus.EXPIRED);
        }

        if (otpData.otp() == null) {
            return OtpVerificationResult.of(OtpVerificationStatus.ALREADY_USED);
        }

        if (!matches(otpData.otp(), otp)) {
            int invalidAttempts = otpData.invalidAttempts() + 1;
            if (invalidAttempts >= MAX_INVALID_ATTEMPTS) {
                otpStore.remove(username);
                return OtpVerificationResult.of(OtpVerificationStatus.TOO_MANY_ATTEMPTS);
            }

            otpStore.put(username, new OtpData(
                    otpData.otp(),
                    otpData.otpExpiry(),
                    otpData.resendAvailableAt(),
                    otpData.resetToken(),
                    otpData.resetTokenExpiry(),
                    invalidAttempts
            ));
            return OtpVerificationResult.of(OtpVerificationStatus.INVALID);
        }

        String resetToken = UUID.randomUUID().toString();
        Instant resetTokenExpiry = now.plus(Duration.ofMinutes(RESET_TOKEN_EXPIRY_MINUTES));
        otpStore.put(username, new OtpData(
                null,
                otpData.otpExpiry(),
                otpData.resendAvailableAt(),
                resetToken,
                resetTokenExpiry,
                otpData.invalidAttempts()
        ));

        return OtpVerificationResult.verified(resetToken, RESET_TOKEN_EXPIRY_MINUTES * 60L);
    }

    /**
     * Consumes a verified reset token. A consumed token cannot reset the password again.
     */
    public synchronized boolean consumePasswordResetToken(String username, String resetToken) {
        OtpData otpData = otpStore.get(username);
        if (otpData == null || otpData.resetToken() == null || otpData.resetTokenExpiry() == null) {
            return false;
        }

        if (!otpData.resetTokenExpiry().isAfter(clock.instant())) {
            otpStore.remove(username);
            return false;
        }

        if (!matches(otpData.resetToken(), resetToken)) {
            return false;
        }

        otpStore.remove(username);
        return true;
    }

    /**
     * Removes a just-issued OTP when email delivery fails, without clearing a newer OTP.
     */
    public synchronized void invalidateIssuedOtp(String username, String otp) {
        OtpData otpData = otpStore.get(username);
        if (otpData != null && matches(otpData.otp(), otp)) {
            otpStore.remove(username);
        }
    }

    private long secondsRemaining(Instant availableAt, Instant now) {
        long millisRemaining = Duration.between(now, availableAt).toMillis();
        return Math.max(1, (millisRemaining + 999) / 1000);
    }

    private boolean matches(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }

    public enum OtpVerificationStatus {
        VERIFIED,
        INVALID,
        EXPIRED,
        NOT_FOUND,
        ALREADY_USED,
        TOO_MANY_ATTEMPTS
    }

    public record OtpIssueResult(boolean issued, String otp, long resendAvailableIn, long otpExpiresIn) {
        private static OtpIssueResult issued(String otp, long resendAvailableIn, long otpExpiresIn) {
            return new OtpIssueResult(true, otp, resendAvailableIn, otpExpiresIn);
        }

        private static OtpIssueResult cooldown(long resendAvailableIn) {
            return new OtpIssueResult(false, null, resendAvailableIn, 0);
        }
    }

    public record OtpVerificationResult(OtpVerificationStatus status, String resetToken, long resetTokenExpiresIn) {
        private static OtpVerificationResult verified(String resetToken, long resetTokenExpiresIn) {
            return new OtpVerificationResult(OtpVerificationStatus.VERIFIED, resetToken, resetTokenExpiresIn);
        }

        private static OtpVerificationResult of(OtpVerificationStatus status) {
            return new OtpVerificationResult(status, null, 0);
        }
    }

    private record OtpData(
            String otp,
            Instant otpExpiry,
            Instant resendAvailableAt,
            String resetToken,
            Instant resetTokenExpiry,
            int invalidAttempts
    ) {
    }
}
