package com.app.mybank.infastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtService {

    private static final Key SECRET_KEY = Jwts.SIG.HS256.key().build();
    private static final long   EXP_MS = 1000 * 60 * 60;   // 1 h

    public String generateToken(UserDetails ud) {
        Date now = new Date();
        return Jwts.builder()
                .subject(ud.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXP_MS))
                .signWith(SECRET_KEY)
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getPayload().getSubject();
    }

    public boolean isTokenValid(String token, UserDetails ud) {
        Claims c = parse(token).getPayload();
        return c.getSubject().equals(ud.getUsername())
                && c.getExpiration().after(new Date());
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) SECRET_KEY)
                .build()
                .parseSignedClaims(token);
    }
}

