package com.pvt.project71.controllers;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.mappers.mapperimpl.UserMapperImpl;
import com.pvt.project71.services.security.GoogleAuthService;
import com.pvt.project71.services.security.JwtService;
import com.pvt.project71.services.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

@RestController
public class LoginController {

    private final UserService userService;
    private final UserMapperImpl userMapper;
    private final JwtService jwtService;
    private final GoogleAuthService googleTokenService;
    public LoginController(UserService userService, UserMapperImpl userMapper, JwtService service, GoogleAuthService googleTokenService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.jwtService = service;
        this.googleTokenService = googleTokenService;
    }

    @GetMapping("/login/cred/{email}/{password}")
    public ResponseEntity<String> login(@NotBlank @Email @PathVariable String email, @PathVariable String password){
        Optional<UserEntity> userOpt = userService.findOne(email);
        if (userOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        UserEntity user = userOpt.get();
        String token = jwtService.generateTokenFromUserEntity(user,1, ChronoUnit.HOURS).getTokenValue();
        if (user.getPassword() == null) {
            user.setPassword(password);
            user = userService.save(user);
            return new ResponseEntity<>(token,HttpStatus.OK);
        } else   if (!user.getPassword().equals(password))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(token,HttpStatus.OK);
    }
    @GetMapping("/login/google/{email}")
    public ResponseEntity<String> loginWithToken(@RequestHeader("GAuth") String gAuth,@PathVariable  String email){
         if (gAuth == null || !gAuth.startsWith("Bearer"))
             return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        GoogleIdToken googleToken = googleTokenService.verifyToken(gAuth.substring(7));
        if (googleToken == null || !googleToken.getPayload().getEmail().equalsIgnoreCase(email))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        Optional<UserEntity> userOpt = userService.findOne(email);
        if (userOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        UserEntity user = userOpt.get();
        String jwt = jwtService.generateTokenFromUserEntity(user,1, ChronoUnit.HOURS).getTokenValue();
        return new ResponseEntity<>(jwt,HttpStatus.OK);
    }

}


