package co.com.bancolombia.security.mock.userToken.gateways;

import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.domain.userToken.UserTokenModel;
import reactor.core.publisher.Mono;

public interface UserTokenRepository {
    Mono<UserTokenModel> save(UserTokenModel passwordResetModel);
    Mono<UserTokenModel> findByTokenAndType(String token , TokenType tokenType);
    Mono<Void> markAsUsed(UserTokenModel userToken);

}
