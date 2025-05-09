package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.FriendshipDto;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.FriendshipEntity;
import com.pvt.project71.domain.entities.FriendshipId;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.mappers.Mapper;
import com.pvt.project71.services.FriendshipService;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/*
TODO: Fixa resten av controllern (Accept, delete osv)
*/

@RestController
public class FriendshipController {

    private final JwtService jwtService;
    private FriendshipService friendshipService;
    private UserService userService;

    private Mapper<FriendshipEntity, FriendshipDto> friendshipMapper;

    public FriendshipController(FriendshipService friendshipService, Mapper<FriendshipEntity, FriendshipDto> friendshipMapper, JwtService jwtService, UserService userService) {
        this.friendshipService = friendshipService;
        this.friendshipMapper = friendshipMapper;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping(path = "/friends/request")
    public ResponseEntity<FriendshipDto> createUser(@RequestBody UserDto receiver,
                                              @AuthenticationPrincipal Jwt userToken){

        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if(!userService.isExists(receiver.getEmail()) || !userService.isExists(userToken.getSubject())){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<UserEntity> requester = userService.findOne(userToken.getSubject());
        Optional<UserEntity> accepter = userService.findOne(receiver.getEmail());

        FriendshipEntity friendship = FriendshipEntity.builder()
                        .id(new FriendshipId(userToken.getSubject(), receiver.getEmail()))
                        .requester(requester.get())
                        .receiver(accepter.get())
                        .status(Status.PENDING)
                        .build();

        FriendshipEntity savedFriendship = friendshipService.save(friendship);
        return new ResponseEntity<>(friendshipMapper.mapTo(savedFriendship), HttpStatus.CREATED);
    }



}
