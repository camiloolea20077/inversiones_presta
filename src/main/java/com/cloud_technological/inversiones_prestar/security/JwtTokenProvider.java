package com.cloud_technological.inversiones_prestar.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.cloud_technological.inversiones_prestar.utils.GlobalException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-milliseconds}")
    private long jwtExpirationDate;

    /** Genera el token incluyendo el id del usuario y su rol como claims. */
    public String generateToken(Authentication authentication, Long usuarioId, String rol) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        Map<String, Object> claims = new HashMap<>();
        claims.put("usuarioId", usuarioId);
        claims.put("rol", rol);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUsuarioId(String token) {
        Object value = getClaims(token).get("usuarioId");
        if (value == null) {
            return null;
        }
        return value instanceof Long ? (Long) value : Long.valueOf(value.toString());
    }

    public String getRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Token JWT inválido");
        } catch (ExpiredJwtException e) {
            throw new GlobalException(HttpStatus.UNAUTHORIZED, "Token JWT expirado");
        } catch (UnsupportedJwtException e) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Token JWT no soportado");
        } catch (IllegalArgumentException e) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La cadena claims JWT está vacía");
        }
    }
}
