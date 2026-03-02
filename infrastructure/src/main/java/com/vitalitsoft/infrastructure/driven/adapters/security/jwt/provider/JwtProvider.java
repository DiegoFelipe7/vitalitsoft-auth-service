package com.vitalitsoft.infrastructure.driven.adapters.security.jwt.provider;

import com.vitalitsoft.domain.shared.enums.JwtType;
import com.vitalitsoft.domain.shared.enums.TokenType;
import com.vitalitsoft.infrastructure.driven.adapters.security.config.model.SecurityProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private static final Logger LOGGER = Logger.getLogger(JwtProvider.class.getName());
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
            LOGGER.severe("token expired");
        } catch (UnsupportedJwtException e) {
            LOGGER.severe("token unsupported");
        } catch (MalformedJwtException e) {
            LOGGER.severe("token malformed");
        } catch (SignatureException e) {
            LOGGER.severe("bad signature");
        } catch (IllegalArgumentException e) {
            LOGGER.severe("illegal args");
        }
        return false;
    }

    private SecretKey getKey(String secret) {
        byte[] secretBytes = Decoders.BASE64URL.decode(secret);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}