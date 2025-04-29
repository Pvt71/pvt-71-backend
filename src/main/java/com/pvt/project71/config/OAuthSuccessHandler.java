package com.pvt.project71.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.services.JWTService; // import your service
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OAuthSuccessHandler(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    /*
    After successful OAuth2, send back JWT.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String token = jwtService.generateToken(authentication);
        //Send JWT
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), Map.of("token", token));
    }
}
