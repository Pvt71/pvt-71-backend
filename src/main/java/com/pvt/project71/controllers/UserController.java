package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.domain.entities.score.ScoreId;
import com.pvt.project71.mappers.Mapper;
import com.pvt.project71.mappers.mapperimpl.UserMapperImpl;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.ScoreService;
import com.pvt.project71.services.UserService;
import jakarta.validation.Path;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class UserController {

    private final JwtService jwtService;
    private UserService userService;


    private Mapper<UserEntity, UserDto> userMapper;

    public UserController(UserService userService, Mapper<UserEntity, UserDto> userMapper, JwtService jwtService){
        this.userService = userService;
        this.userMapper = userMapper;
        this.jwtService = jwtService;

    }

    // CRUD - Create
    @PostMapping(path = "/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto user,
                                              @AuthenticationPrincipal Jwt userToken){

        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        user.setEmail(userToken.getSubject());
        UserEntity userEntity = userMapper.mapFrom(user);
        UserEntity savedUserEntity = userService.save(userEntity);
        return new ResponseEntity<>(userMapper.mapTo(savedUserEntity), HttpStatus.CREATED);
    }

    @GetMapping(path = "/users")
    public ResponseEntity<?> listUsers() {
        List<UserEntity> users = userService.findAll();
        List<UserDto> userDtos = users.stream()
                .map(userMapper::mapTo)
                .collect(Collectors.toList());
        return new ResponseEntity<>(userDtos, HttpStatus.OK);
    }

    // CRUD - Read (one)
    @GetMapping(path = "/users/{email}")
    public ResponseEntity<UserDto> getUser(@PathVariable("email") String email){
        Optional<UserEntity> foundUser = userService.findOne(email);
        return foundUser.map(userEntity -> {
            UserDto userDto = userMapper.mapTo(userEntity);
            return new ResponseEntity<>(userDto,HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @GetMapping(path = "/users/myProfile")
    public ResponseEntity<UserDto> getOwnUser(@AuthenticationPrincipal Jwt userToken) {
        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> foundUser = userService.findOne(userToken.getSubject());
        if (foundUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        }
        return new ResponseEntity<>(userMapper.mapTo(foundUser.get()), HttpStatus.OK);
    }

    // CRUD - Update (full update)
    @PutMapping(path = "/users")
    public ResponseEntity<UserDto> fullUpdateUser(
            @Valid @RequestBody UserDto userDto,
            @AuthenticationPrincipal Jwt userToken){

        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String emailFromToken = userToken.getSubject();
        Optional<UserEntity> existingUserOpt = userService.findOne(emailFromToken);
        if (existingUserOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        UserEntity existingUser = existingUserOpt.get();

        existingUser.setUsername(userDto.getUsername());
        existingUser.setSchool(userDto.getSchool());

        UserEntity savedUserEntity = userService.save(existingUser);

        return new ResponseEntity<>(
                userMapper.mapTo(savedUserEntity),
                HttpStatus.OK
        );
    }

    // CRUD - Update (partial update)
    @PatchMapping(path = "/users")
    public ResponseEntity<UserDto> partialUpdate(
            @Valid @RequestBody UserDto userDto,
            @AuthenticationPrincipal Jwt userToken
    ){
        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String emailFromToken = userToken.getSubject();
        if(!userService.isExists(emailFromToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        userDto.setEmail(emailFromToken);
        UserEntity userEntity = userMapper.mapFrom(userDto);
        UserEntity updatedUser = userService.partialUpdate(emailFromToken, userEntity);
        return new ResponseEntity<>(userMapper.mapTo(updatedUser), HttpStatus.OK);
    }

    // CRUD - Delete
    @DeleteMapping(path = "/users")
    public ResponseEntity deleteUser(@AuthenticationPrincipal Jwt userToken){

        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String emailFromToken = userToken.getSubject();
        if(!userService.isExists(emailFromToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        userService.delete(emailFromToken);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
