package com.vitalitsoft.domain.auth;

import com.vitalitsoft.domain.shared.constants.HttpStatus;
import com.vitalitsoft.domain.shared.enums.Role;
import com.vitalitsoft.domain.shared.enums.Status;
import com.vitalitsoft.domain.shared.exception.NexusException;
import lombok.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthModel {
    private UUID id;
    private String email;
    private String password;
    private Role role;
    private Status status;
    private LocalDate twoFactorNotRequiredUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public boolean requiresTwoFactor() {

        if (twoFactorNotRequiredUntil == null) {
            return true;
        }
        return LocalDate.now().isAfter(twoFactorNotRequiredUntil);
    }


    public void ensureCanLogin() {
         switch (this.status) {
            case INACTIVE -> {
                throw new NexusException(
                        NexusException.Type.ACCOUNT_LOCKED,
                        HttpStatus.FORBIDDEN
                );
            }
            case PENDING_VERIFICATION -> {
                throw new NexusException(
                        NexusException.Type.PENDING_VERIFICATION,
                        HttpStatus.FORBIDDEN
                );
            }
        };
    }
}
