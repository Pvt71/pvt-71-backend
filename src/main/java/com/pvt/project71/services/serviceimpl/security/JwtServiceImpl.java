package com.pvt.project71.services.serviceimpl.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.services.security.GoogleAuthService;
import com.pvt.project71.services.security.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
public class JwtServiceImpl implements JwtService {
    private final GoogleAuthService googleAuthService;
    private final JwtEncoder encoder;

    public JwtServiceImpl(GoogleAuthService googleAuthService, JwtEncoder encoder) {
        this.googleAuthService = googleAuthService;
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
    @Override
     public Jwt generateTokenFromUserEntity(UserEntity userEntity, long duration, ChronoUnit timeUnit) {

        Instant time = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("self")
                .issuedAt(time)
                .expiresAt(time.plus(duration,timeUnit))
                .subject(userEntity.getEmail())
                .build();
        return this.encoder.encode(JwtEncoderParameters.from(claims));
    }

    @Override
    public Jwt generateJwtFromGoogle(String googleToken) {

        GoogleIdToken idToken = googleAuthService.verifyToken(googleToken);

        if (googleToken != null) {
            UserEntity user = UserEntity.builder().email(idToken.getPayload().getEmail()).build();
            return generateTokenFromUserEntity(user, 20,ChronoUnit.MINUTES);
        }
        return null;
    }

    @Override
    public boolean isTokenValid(Jwt jwt) {
        if (jwt != null) {
            return jwt.getExpiresAt().isAfter(Instant.now());
        }
        return false;
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
