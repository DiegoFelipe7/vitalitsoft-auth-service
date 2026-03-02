package co.com.bancolombia.security.mock.events.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record SendOtpEventModel(String otp, String email){}
