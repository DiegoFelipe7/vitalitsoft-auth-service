package co.com.bancolombia.security.mock.refreshtoken.gateways;

import com.vitalitsoft.domain.refreshtoken.RefreshTokenModel;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RefreshTokenRepository {
    Mono<RefreshTokenModel> findByToken(String token);
    Mono<Void> rotate(String email, UUID userId , String token);
    Mono<Void> revokeTokenByUserId(UUID userId);
}
