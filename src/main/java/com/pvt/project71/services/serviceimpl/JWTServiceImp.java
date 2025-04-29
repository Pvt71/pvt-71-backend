package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.services.JWTService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
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
public class JWTServiceImp implements JWTService {
    private final JwtEncoder encoder;
    public JWTServiceImp(JwtEncoder encoder) {
        this.encoder = encoder;
    }
    @Override
    public String generateToken(Authentication authentication) {

        String subject = getEmail(authentication);
        Instant time = Instant.now();
        //Permissions are derived from Authentication, which,
        // in this case is just read permissions as we have not setup any permission logiccs
        String jwtScope = authentication.getAuthorities().
                stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("self")
                .issuedAt(time)
                .expiresAt(time.plus(1, ChronoUnit.HOURS))
                .subject(getEmail(authentication))
                .claim("scope", jwtScope)
                .build();
        //TODO: Ask front-end, Daniel, Patrik and Andreas if any other fields are needed.
        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
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
