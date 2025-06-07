package com.bowlingpoints.config.jwt;

import com.bowlingpoints.entity.RolePermission;
import com.bowlingpoints.entity.User;
import com.bowlingpoints.entity.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final String SECRET_KEY = "586E3272357538782F413F4428472B4B6250655368566B597033733676397924";

    public String getToken(User user) {
        return getToken(new HashMap<>(), user);
    }

    private String getToken(Map<String, Object> extraClaims, User user) {
        long expirationMs = 36000000; // 1 hora

        // Roles
        List<String> roles = user.getUserRoles().stream()
                .filter(UserRole::isGranted)
                .map(userRole -> userRole.getRole().getDescription())
                .collect(Collectors.toList());

        // Permisos
        List<String> permissions = user.getUserRoles().stream()
                .flatMap(userRole -> userRole.getRole().getRolePermissions().stream())
                .filter(RolePermission::getGranted)
                .map(rp -> rp.getPermission().getName())
                .distinct()
                .collect(Collectors.toList());

        // Claims personalizados
        extraClaims.put("roles", roles);
        extraClaims.put("permissions", permissions);
        extraClaims.put("correo", user.getPerson().getEmail());

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getNickname())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Claims getAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date getExpiration(String token) {
        try {
            return getClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo obtener la expiración del token: " + e.getMessage());
            return null;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = getExpiration(token);
        if (expiration == null) {
            return false; // ⚠️ Asumimos que si no hay expiración, el token es válido
        }
        return expiration.before(new Date());
    }
}
