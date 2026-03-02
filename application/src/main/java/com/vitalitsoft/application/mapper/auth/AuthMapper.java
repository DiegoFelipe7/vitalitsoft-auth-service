package com.vitalitsoft.application.mapper.auth;



import com.vitalitsoft.application.dto.auth.request.RegisterUserRequest;
import com.vitalitsoft.domain.auth.AuthModel;
import com.vitalitsoft.domain.events.model.UserRegisterEventModel;
import com.vitalitsoft.domain.shared.enums.Role;
import com.vitalitsoft.domain.shared.enums.Status;

import java.time.LocalDateTime;

public final class AuthMapper {

    private AuthMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static AuthModel toAuthModel(String email, String password) {
        return AuthModel.builder()
                .email(email)
                .password(password)
                .role(Role.USER)
                .status(Status.PENDING_VERIFICATION)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }



    public static UserRegisterEventModel toUserRegisterEventModel(RegisterUserRequest request) {
        return UserRegisterEventModel.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }



}
