package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.mappers.Mapper;
import com.pvt.project71.mappers.mapperimpl.UserMapperImpl;
import com.pvt.project71.services.UserService;
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

    private UserService userService;

    private Mapper<UserEntity, UserDto> userMapper;

    public UserController(UserService userService, Mapper<UserEntity, UserDto> userMapper){
        this.userService = userService;
        this.userMapper = userMapper;
    }

    // CRUD - Create
    @PostMapping(path = "/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto user,
                                              @AuthenticationPrincipal Jwt userToken){
        if(userToken == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserEntity userEntity = userMapper.mapFrom(user);
        UserEntity savedUserEntity = userService.save(userEntity);
        return new ResponseEntity<>(userMapper.mapTo(savedUserEntity), HttpStatus.CREATED);
    }

    // CRUD - Read (many)
    @GetMapping(path = "/users")
    public List<UserDto> listUsers(){
        List<UserEntity> users = userService.findAll();
        return users.stream()
                .map(userMapper::mapTo)
                .collect(Collectors.toList());
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

    // CRUD - Update (full update)
    @PutMapping(path = "/users")
    public ResponseEntity<UserDto> fullUpdateUser(
            @Valid @RequestBody UserDto userDto,
            @AuthenticationPrincipal Jwt userToken){

        if(userToken == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String emailFromToken = userToken.getSubject();
        if(!userService.isExists(emailFromToken)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        userDto.setEmail(emailFromToken);
        UserEntity userEntity = userMapper.mapFrom(userDto);
        UserEntity savedUserEntity = userService.save(userEntity);
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
        if(userToken == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String emailFromToken = userToken.getSubject();
        if(!userService.isExists(emailFromToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserEntity userEntity = userMapper.mapFrom(userDto);
        UserEntity updatedUser = userService.partialUpdate(emailFromToken, userEntity);
        return new ResponseEntity<>(userMapper.mapTo(updatedUser), HttpStatus.OK);
    }

    // CRUD - Delete
    @DeleteMapping(path = "/users")
    public ResponseEntity deleteUser(@AuthenticationPrincipal Jwt userToken){

        if(userToken == null){
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
