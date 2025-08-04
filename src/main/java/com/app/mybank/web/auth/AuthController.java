package com.app.mybank.web.auth;

import com.app.mybank.infastructure.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final PasswordEncoder encoder;

    public AuthController(AuthenticationManager authManager, JwtService jwt, PasswordEncoder encoder) {
        this.authManager = authManager;
        this.jwt = jwt;
        this.encoder = encoder;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        UserDetails ud = (UserDetails) auth.getPrincipal();
        return new TokenResponse(jwt.generateToken(ud));
    }

    record LoginRequest(@NotBlank String email,
                        @NotBlank String password) {}

    record TokenResponse(String token) {}
}

