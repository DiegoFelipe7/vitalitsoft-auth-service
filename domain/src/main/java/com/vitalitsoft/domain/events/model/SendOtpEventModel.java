package com.vitalitsoft.domain.events.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record SendOtpEventModel(String otp, String sessionId, String email){}
