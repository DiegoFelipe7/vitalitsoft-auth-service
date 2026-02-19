package com.vitalitsoft.infrastructure.driven.adapters.security.jwt.provider;

import com.vitalitsoft.infrastructure.driven.adapters.security.config.model.SecurityProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private static final Logger LOGGER = Logger.getLogger(JwtProvider.class.getName());
    private final SecurityProperties securityProperties;


    public String generateAccessToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities())
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + securityProperties.expiration()))
                .signWith(getKey(securityProperties.secret()))
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + (30L * 24 * 60 * 60 * 1000)))
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