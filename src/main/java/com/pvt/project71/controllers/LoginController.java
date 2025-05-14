package com.pvt.project71.controllers;


import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.mappers.mapperimpl.UserMapperImpl;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.UserService;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@RestController
public class LoginController {

    private final UserService userService;
    private final UserMapperImpl userMapper;
    private final JwtService jwtService;
    public LoginController(UserService userService, UserMapperImpl userMapper, JwtService service) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.jwtService = service;
    }

    @GetMapping("/login/cred/{email}/{password}")
    public ResponseEntity<String> login(@NotBlank @Email @PathVariable String email, @PathVariable String password){
        Optional<UserEntity> userOpt = userService.findOne(email);
        if (userOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        UserEntity user = userOpt.get();
        String token = jwtService.mockOauth2(user,1, ChronoUnit.HOURS).getTokenValue();
        if (user.getPassword() == null) {
            user.setPassword(password);
            user = userService.save(user);
            return new ResponseEntity<>(token,HttpStatus.OK);
        } else   if (!user.getPassword().equals(password))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(token,HttpStatus.OK);
    }

}
