package com.example.demo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtService {

    private final SecretKey key;
    private final long accessTtlMs;

    public JwtService(
            @Value("${security.jwt.secret-base64}") String base64Secret,
            @Value("${security.jwt.access-ttl-ms:3600000}") long accessTtlMs // default 1h
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.accessTtlMs = accessTtlMs;
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessTtlMs))
                .signWith(key, SignatureAlgorithm.HS256) // 0.11.5 style
                .compact();
    }

    public Jws<Claims> parse(String token) {
        // 0.11.5 parser API
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public String getSubject(String token) {
        return parse(token).getBody().getSubject();
    }

    public boolean isExpired(String token) {
        return parse(token).getBody().getExpiration().before(new Date());
    }
}
