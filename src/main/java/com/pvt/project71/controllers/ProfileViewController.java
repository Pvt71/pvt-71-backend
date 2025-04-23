package com.pvt.project71.controllers;


import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileViewController {
    @GetMapping("/view-profile")
    public String profile(OAuth2AuthenticationToken  token, Model model) {
        /*
        When debugging is needed (trust me, you will need this...)
                debugProfileViewer(token);
         */

        model.addAttribute("name", token.getPrincipal().getAttribute("name"));
        model.addAttribute("email", token.getPrincipal().getAttribute("email"));
        model.addAttribute("picture", token.getPrincipal().getAttribute("picture"));

        System.out.println("Rendering template: profilex.html");
        return "profilex"; // Case-sensitive!
    }
    private void debugProfileViewer(OAuth2AuthenticationToken  token){
        System.out.println("=== OAuth2 Attributes ===");
        token.getPrincipal().getAttributes().forEach((k,v) ->
                System.out.println(k + ": " + v));
    }
}
