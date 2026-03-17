package com.vitalitsoft.domain.auth;

import com.vitalitsoft.domain.shared.enums.Role;
import com.vitalitsoft.domain.shared.enums.Status;
import com.vitalitsoft.domain.shared.exception.VitalitSoftException;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@With
@Builder(toBuilder = true)
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
            case INACTIVE ->
                throw new VitalitSoftException(VitalitSoftException.Type.ACCOUNT_LOCKED);

            case PENDING_VERIFICATION ->
                throw new VitalitSoftException(VitalitSoftException.Type.PENDING_VERIFICATION);

        }
    }
}
