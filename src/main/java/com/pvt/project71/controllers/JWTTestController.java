package com.pvt.project71.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import com.pvt.project71.services.JWTService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
public class JWTTestController {
    private static final Logger logger = LoggerFactory.getLogger(JWTTestController.class);

    private final JWTService jwtService;

    public JWTTestController(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("req_jwt")
    public String onRegister(@AuthenticationPrincipal Jwt jwt){
        return  "I see thy, " + jwt.getSubject() + ", embracing that despicable flame of ambition - King of all Fell Omen";
    }
}
