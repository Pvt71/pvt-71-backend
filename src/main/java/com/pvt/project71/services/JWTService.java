package com.pvt.project71.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtEncoder;

public interface JWTService {

      String generateToken(Authentication authentication);
}
