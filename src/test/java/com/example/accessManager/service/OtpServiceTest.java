package com.example.accessManager.service;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class OtpServiceTest {

    @Test
    void blocksResendsDuringCooldownAndInvalidatesThePreviousOtpAfterResend() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-14T00:00:00Z"));
        OtpService otpService = new OtpService(clock, new SequenceSecureRandom(123456, 654321));

        OtpService.OtpIssueResult firstIssue = otpService.issueOtp("alex");
        OtpService.OtpIssueResult blockedResend = otpService.issueOtp("alex");

        assertThat(firstIssue.issued()).isTrue();
        assertThat(blockedResend.issued()).isFalse();
        assertThat(blockedResend.resendAvailableIn()).isEqualTo(60);

        clock.advanceSeconds(60);
        OtpService.OtpIssueResult resend = otpService.issueOtp("alex");

        assertThat(resend.issued()).isTrue();
        assertThat(resend.otp()).isNotEqualTo(firstIssue.otp());
        assertThat(otpService.verifyOtp("alex", firstIssue.otp()).status())
                .isEqualTo(OtpService.OtpVerificationStatus.INVALID);
    }

    @Test
    void exchangesAValidOtpForASingleUseResetToken() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-14T00:00:00Z"));
        OtpService otpService = new OtpService(clock, new SequenceSecureRandom(123456));

        OtpService.OtpIssueResult issue = otpService.issueOtp("alex");
        OtpService.OtpVerificationResult verification = otpService.verifyOtp("alex", issue.otp());

        assertThat(verification.status()).isEqualTo(OtpService.OtpVerificationStatus.VERIFIED);
        assertThat(verification.resetToken()).isNotBlank();
        assertThat(otpService.consumePasswordResetToken("alex", verification.resetToken())).isTrue();
        assertThat(otpService.consumePasswordResetToken("alex", verification.resetToken())).isFalse();
    }

    @Test
    void rejectsAnExpiredOtp() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-14T00:00:00Z"));
        OtpService otpService = new OtpService(clock, new SequenceSecureRandom(123456));

        OtpService.OtpIssueResult issue = otpService.issueOtp("alex");
        clock.advanceSeconds(5 * 60);

        assertThat(otpService.verifyOtp("alex", issue.otp()).status())
                .isEqualTo(OtpService.OtpVerificationStatus.EXPIRED);
    }

    @Test
    void invalidatesTheOtpAfterFiveIncorrectAttempts() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-14T00:00:00Z"));
        OtpService otpService = new OtpService(clock, new SequenceSecureRandom(123456));

        otpService.issueOtp("alex");
        for (int attempt = 0; attempt < 4; attempt++) {
            assertThat(otpService.verifyOtp("alex", "000000").status())
                    .isEqualTo(OtpService.OtpVerificationStatus.INVALID);
        }

        assertThat(otpService.verifyOtp("alex", "000000").status())
                .isEqualTo(OtpService.OtpVerificationStatus.TOO_MANY_ATTEMPTS);
        assertThat(otpService.verifyOtp("alex", "123456").status())
                .isEqualTo(OtpService.OtpVerificationStatus.NOT_FOUND);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advanceSeconds(long seconds) {
            instant = instant.plusSeconds(seconds);
        }
    }

    private static final class SequenceSecureRandom extends SecureRandom {
        private final int[] values;
        private int index;

        private SequenceSecureRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            int value = values[Math.min(index, values.length - 1)];
            index++;
            return value % bound;
        }
    }
}
