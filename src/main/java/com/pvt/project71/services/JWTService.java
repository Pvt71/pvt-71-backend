package com.pvt.project71.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.temporal.ChronoUnit;

public interface JWTService {

      Jwt generateToken(Authentication authentication, long duration, ChronoUnit timeUnit);
}
