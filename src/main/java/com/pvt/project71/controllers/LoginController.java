package com.pvt.project71.controllers;


import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.mappers.mapperimpl.UserMapperImpl;
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
import java.util.Optional;

@RestController
public class LoginController {

    private final UserService userService;
    private final UserMapperImpl userMapper;
    public LoginController(UserService userService, UserMapperImpl userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/login/cred/{email}/{password}")
    public ResponseEntity<UserDto> login(@NotBlank @Email @PathVariable String email, @PathVariable String password){
        Optional<UserEntity> userOpt = userService.findOne(email);
        if (userOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        UserEntity user = userOpt.get();

        if (user.getPassword() == null) {
            user.setPassword(password);
            user = userService.save(user);
            return new ResponseEntity<>(userMapper.mapTo(user),HttpStatus.OK);

        } else   if (!user.getPassword().equals(password))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(userMapper.mapTo(user),HttpStatus.OK);
    }

}
