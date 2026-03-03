package com.vitalitsoft.domain.shared.utils;

import java.security.SecureRandom;

public class OtpGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_LENGTH = 6;

    private OtpGenerator() {
        throw new IllegalStateException("Utility class");
    }

    public static String generateNumericOtp() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int number = secureRandom.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", number);
    }
}
