package com.pvt.project71.controllers;

import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.services.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController // Replaces @Controller (disables Thymeleaf)
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }



    @GetMapping("/login/oauth2")
    public String login(OAuth2AuthenticationToken token) {
        if (token == null) {
            return "redirect:/register";
        }


        String email = token.getPrincipal().getAttribute("email");
        Optional<UserEntity> userOpt = userService.findOne(email);
        if (userOpt.isEmpty()) {
            return "redirect:/register";
        }
        UserEntity user = userOpt.get();
        authenticateUser(user);
        return user.toString();
    }


    @GetMapping("/oauth2_session")
    public String session(OAuth2AuthenticationToken token) {
        if (token == null) {
            return "ERROR: No OAuth2 session. Redirect to /register";
        }
        String email = token.getPrincipal().getAttribute("email");
        Optional<UserEntity> userOpt = userService.findOne(email);

        if (userOpt.isPresent()) {
            authenticateUser(userOpt.get());
            return "redirect:/view-profilee";
        }

        return userOpt.get().toString();
    }

//TODO: See UserDetailService task in SecurityConfig
    private void authenticateUser(UserEntity userEntity) {
        String rawPassword = userEntity.getPassword();
        Authentication authRequest = new UsernamePasswordAuthenticationToken(
                userEntity.getEmail(),
                rawPassword // or maybe hashed? I am a bit lost at this point
        );
        Authentication authResponse = authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authResponse);
    }

}