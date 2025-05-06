package com.pvt.project71.services;

import com.pvt.project71.domain.entities.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.temporal.ChronoUnit;

public interface JwtService {

      Jwt generateToken(Authentication authentication, long duration, ChronoUnit timeUnit);
      Jwt mockOauth2(UserEntity userEntity,long duration, ChronoUnit timeUnit);
}
