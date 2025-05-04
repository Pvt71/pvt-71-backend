package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.services.JWTService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
public class JwtServiceImpl implements JWTService {
    private final JwtEncoder encoder;
    public JwtServiceImpl(JwtEncoder encoder) {
        this.encoder = encoder;
    }
    @Override

    public Jwt generateToken(Authentication authentication, long duration, ChronoUnit timeUnit) {
        Instant time = Instant.now();
        //Permissions are derived from Authentication, which,
        // in this case is just read permissions as we have not setup any permission logiccs
        String jwtScope = authentication.getAuthorities().
                stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("self")
                .issuedAt(time)
                .expiresAt(time.plus(duration,timeUnit))
                .subject(getEmail(authentication))
                .claim("scope", jwtScope)
                .build();
        return this.encoder.encode(JwtEncoderParameters.from(claims));
    }
    //NO AUTHENTICATION, USE WITH CAUTION AND NOT IN PRODUCTION!
    public Jwt generateToken(UserEntity userEntity) {
        Instant time = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("self")
                .issuedAt(time)
                .expiresAt(time.plus(1, ChronoUnit.HOURS))
                .subject(userEntity.getEmail())
                .build();
        return this.encoder.encode(JwtEncoderParameters.from(claims));
    }
    //Pretty sure all OAuth2 verifications create an OAuth2User instance
    //so there should always be an e-mail.

    private String getEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            Object email = oauth2User.getAttribute("email");
            if (email != null) {
                return email.toString();
            }
        }
        return "";
    }

}
