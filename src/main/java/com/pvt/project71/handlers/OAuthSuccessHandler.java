package com.pvt.project71.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.services.JwtService; // import your service
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String JSON_TYPE = "application/json";
    private static final String CALLBACK_SCHEME = "token";
    public OAuthSuccessHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /*
    After successful OAuth2, send back JWT.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
<<<<<<< HEAD
        String token = jwtService.generateToken(authentication, 1, ChronoUnit.DAYS).getTokenValue();
        //Send JWT back to OAuth2 process
      //  objectMapper.writeValue(response.getWriter(), Map.of("token", token));
=======
        String token = jwtService.generateToken(authentication, 20, ChronoUnit.DAYS).getTokenValue();
        //Send JWT back to OAuth2 process
        //objectMapper.writeValue(response.getWriter(), Map.of("token", token));
>>>>>>> cba19cdc33067c68c3031cc049d487e70fb77bb4
        String redirectPath = "/oauth-callback?token=" + URLEncoder.encode(token, "UTF-8");

        response.sendRedirect(redirectPath);
    }
}