package com.pvt.project71.controllers;

import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class ProfileViewController {
    private final UserService userService;

    public ProfileViewController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/view-profile")
    public String showProfile(Authentication authentication, Model model) {
        String email = null;
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            email = oauthToken.getPrincipal().getAttribute("email");
        } else if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        if (email == null) {
            return "redirect:/login";
        }
        Optional<UserEntity> userOpt = userService.findOne(email);
        if (userOpt.isEmpty()) {
            return "redirect:/register"; // User not registered
        }
        UserEntity user = userOpt.get();
        model.addAttribute("name", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("picture", user.getProfilePictureUrl());
        return "profile";
    }
}