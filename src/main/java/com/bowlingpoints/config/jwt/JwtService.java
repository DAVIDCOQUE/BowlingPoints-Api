package com.bowlingpoints.config.jwt;

import com.bowlingpoints.entity.User;
import com.bowlingpoints.entity.UserRole;
import com.bowlingpoints.service.AuthService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);


    public String getToken(User user) {

        String token = getToken(new HashMap<>(), user);

        log.info("Se devuelve la informacion con el token->{}",token);

        return token;
    }

    private String getToken(Map<String, Object> extraClaims, User user) {
        long expirationMs = jwtConfig.getExpiration();
        log.info("Se crea un nuevo token JWT para el inicio de sesion");

        // Roles
        List<String> roles = user.getUserRoles().stream()
                .filter(UserRole::isGranted)
                .map(userRole -> userRole.getRole().getName())
                .collect(Collectors.toList());
        log.info("Aqui estan los roles->{}",roles);

        extraClaims.put("roles", roles);
        extraClaims.put("correo", user.getPerson().getEmail());

        log.info("Aqui estan los claims->{}",extraClaims);


        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getNickname())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getSecret());
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
