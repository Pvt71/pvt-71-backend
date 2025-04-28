package com.pvt.project71.controllers;

import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.services.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller

public class RegisterController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public RegisterController(UserService userService, AuthenticationManager authenticationManager,
                              PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserEntity());
        return "register";
    }

    //TODO: See UserDetailService task in SecurityConfig
    @PostMapping("/register")
    public String registerUser(
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(value = "school", required = false) String school) {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);
        userEntity.setUsername(username);
        userEntity.setPassword(passwordEncoder.encode(password));
        userEntity.setSchool(school);
        authenticateUser(userEntity);
        userService.save(userEntity);        return "redirect:/view-profile";
    }
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
