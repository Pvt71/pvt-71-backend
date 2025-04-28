package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.services.JWTService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
public class JWTServiceImp implements JWTService {
    private final JwtEncoder encoder;
    public JWTServiceImp(JwtEncoder encoder) {
        this.encoder = encoder;
    }
    @Override
    public String generateToken(Authentication authentication) {

        Instant time = Instant.now();
        String jwtScope = authentication.getAuthorities().
                stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("self")
                .issuedAt(time)
                .expiresAt(time.plus(1, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .claim("scope", jwtScope)
                .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
