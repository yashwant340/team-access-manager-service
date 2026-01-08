package com.example.accessManager.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final int EXPIRY_MINUTES = 5;
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    public String generateOtp(String username) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(username, new OtpData(otp, LocalDateTime.now().plusMinutes(EXPIRY_MINUTES)));
        return otp;
    }

    public boolean validateOtp(String username, String otp) {
        if (!otpStore.containsKey(username)) {
            return false;
        }

        OtpData otpData = otpStore.get(username);
        if (otpData.expiry.isBefore(LocalDateTime.now())) {
            otpStore.remove(username);
            return false; // expired
        }

        boolean isValid = otpData.otp.equals(otp);
        if (isValid) {
            otpStore.remove(username); // one-time use
        }
        return isValid;
    }

    private record OtpData(String otp, LocalDateTime expiry) {}
}

