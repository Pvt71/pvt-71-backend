package com.pvt.project71.controllers;


import com.pvt.project71.services.security.GoogleAuthService;
import com.pvt.project71.services.security.JwtService;
import com.pvt.project71.services.serviceimpl.security.GoogleAuthServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;

@RestController
public class VerificationController {


    private final JwtService jwtService;
    public VerificationController(JwtService jwtService) {

        this.jwtService = jwtService;
    }
    @GetMapping("/google/auth")
    public ResponseEntity<String> verifyGoogleIdToken(@RequestHeader("GAuth") String gAuth) {

        if (gAuth == null ||  !gAuth.startsWith("Bearer"))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
         String token = gAuth.substring(7);
        //Invalid google token returns null
        Jwt jwt = jwtService.generateJwtFromGoogle(token);;
        return  jwt != null ? new ResponseEntity<>(jwt.getTokenValue(),HttpStatus.OK) : new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}

