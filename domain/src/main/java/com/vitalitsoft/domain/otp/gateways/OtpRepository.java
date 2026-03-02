package co.com.bancolombia.security.mock.otp.gateways;

import com.vitalitsoft.domain.otp.OtpModel;
import reactor.core.publisher.Mono;

public interface OtpRepository {
    Mono<OtpModel> save(OtpModel otpModel);
    Mono<OtpModel> findBySessionId(String sessionId);
}
