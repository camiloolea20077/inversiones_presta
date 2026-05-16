package com.cloud_technological.inversiones_prestar.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Acceso a los datos del usuario autenticado a partir del token JWT
 * que viaja en el SecurityContext. Lo usan los servicios cuando necesitan
 * saber quién ejecuta la operación (auditoría, ruta del trabajador, etc.).
 */
@Component
public class SecurityUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public Long getUsuarioId() {
        String token = getTokenFromSecurityContext();
        if (token == null) {
            return null;
        }
        Object value = extractAllClaims(token).get("usuarioId");
        if (value == null) {
            return null;
        }
        return value instanceof Long ? (Long) value : Long.valueOf(value.toString());
    }

    public String getRol() {
        String token = getTokenFromSecurityContext();
        return token != null ? extractAllClaims(token).get("rol", String.class) : null;
    }

    private String getTokenFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        return null;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
