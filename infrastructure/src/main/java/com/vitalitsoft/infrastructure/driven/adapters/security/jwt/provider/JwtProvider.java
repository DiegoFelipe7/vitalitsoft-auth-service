package com.vitalitsoft.infrastructure.driven.adapters.security.jwt.provider;

import com.vitalitsoft.domain.shared.enums.JwtType;
import com.vitalitsoft.infrastructure.driven.adapters.security.config.model.SecurityProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

    private final SecurityProperties securityProperties;


    public String generateAccessToken(UserDetails userDetails, String email) {
        return generateToken(
                userDetails,
                email,
                JwtType.ACCESS,
                securityProperties.expiration()
        );
    }

    public String generateRefreshToken(UserDetails userDetails, String email) {
        return generateToken(
                userDetails,
                email,
                JwtType.REFRESH,
                securityProperties.refreshExpiration()
        );
    }
    private String generateToken(
            UserDetails userDetails,
            String email,
            JwtType type,
            long expiration
    ) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(Map.of(
                        "email", email,
                        "roles", userDetails.getAuthorities(),
                        "type", type.name()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiration)))
                .signWith(getKey(securityProperties.secret()))
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey(securityProperties.secret()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(getKey(securityProperties.secret()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validate(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getKey(securityProperties.secret()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return true;
        } catch (ExpiredJwtException e) {
            log.error("token expired");
        } catch (UnsupportedJwtException e) {
            log.error("token unsupported");
        } catch (MalformedJwtException e) {
            log.error("token malformed");
        } catch (SignatureException e) {
            log.error("bad signature");
        } catch (IllegalArgumentException e) {
            log.error("illegal args");
        }
        return false;
    }

    private SecretKey getKey(String secret) {
        byte[] secretBytes = Decoders.BASE64URL.decode(secret);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}