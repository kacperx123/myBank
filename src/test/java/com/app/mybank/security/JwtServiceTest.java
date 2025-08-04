package com.app.mybank.security;

import com.app.mybank.infastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;


import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    JwtService jwt = new JwtService();

    UserDetails sampleUser =
            User.withUsername("alice@example.com").password("x").roles("USER").build();

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwt.generateToken(sampleUser);

        assertNotNull(token);

        String subject = jwt.extractUsername(token);
        assertEquals("alice@example.com", subject);

        assertTrue(jwt.isTokenValid(token, sampleUser));
    }
}
