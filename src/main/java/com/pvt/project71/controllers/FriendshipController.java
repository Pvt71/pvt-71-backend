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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @PostMapping(path = "/friends/add")
    public ResponseEntity<FriendshipDto> sendFriendRequest(@RequestBody UserDto otherUser,
                                                           @AuthenticationPrincipal Jwt requesterToken){
        if(requesterToken == null || !jwtService.isTokenValid(requesterToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        //Both users must exist
        if(!userService.isExists(otherUser.getEmail()) || !userService.isExists(requesterToken.getSubject())){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Can't add yourself
        if(otherUser.getEmail().equals(requesterToken.getSubject())){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<UserEntity> requester = userService.findOne(requesterToken.getSubject());
        Optional<UserEntity> accepter = userService.findOne(otherUser.getEmail());

        FriendshipEntity friendship = FriendshipEntity.builder()
                        .id(new FriendshipId(requesterToken.getSubject(), otherUser.getEmail()))
                        .requester(requester.get())
                        .receiver(accepter.get())
                        .status(Status.PENDING)
                        .build();

        FriendshipEntity savedFriendship = friendshipService.save(friendship);
        return new ResponseEntity<>(friendshipMapper.mapTo(savedFriendship), HttpStatus.CREATED);
    }

    @PutMapping(path = "/friends/accept")
    public ResponseEntity<FriendshipDto> acceptFriendRequest(@AuthenticationPrincipal Jwt userToken,
                                                             @RequestBody FriendshipDto friendRequest){
        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        //User can't accept requests the user sent, friendships must be pending
        if(userToken.getSubject().equals(friendRequest.getRequester().getEmail()) || friendRequest.getStatus() != Status.PENDING){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        FriendshipEntity friendship = friendshipMapper.mapFrom(friendRequest);
        if(!friendshipService.isExists(friendship.getId())){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        friendship.setStatus(Status.ACCEPTED);
        friendship.setFriendsSince(LocalDate.now());
        return new ResponseEntity<>(friendshipMapper.mapTo(friendshipService.save(friendship)), HttpStatus.OK);
    }

    @GetMapping(path = "/friends/requests")
    public ResponseEntity<?> getFriendRequests(@AuthenticationPrincipal Jwt userToken){
        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<FriendshipEntity> friendships = friendshipService.findAllByStatus(userToken.getSubject(), Status.PENDING);
        List<FriendshipDto> friendshipDtos = friendships.stream()
                .map(friendshipMapper::mapTo)
                .collect(Collectors.toList());
        return new ResponseEntity<>(friendshipDtos, HttpStatus.OK);
    }

    @GetMapping(path = "/friends")
    public ResponseEntity<?> getFriends(@AuthenticationPrincipal Jwt userToken){
        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<FriendshipEntity> friendships = friendshipService.findAllByStatus(userToken.getSubject(), Status.ACCEPTED);
        List<FriendshipDto> friendshipDtos = friendships.stream()
                .map(friendshipMapper::mapTo)
                .collect(Collectors.toList());
        return new ResponseEntity<>(friendshipDtos, HttpStatus.OK);
    }

    // Reject requests and remove friendships
    @DeleteMapping(path = "/friends")
    public ResponseEntity<FriendshipDto> deleteFriendship(@AuthenticationPrincipal Jwt userToken,
                                                             @RequestBody FriendshipDto friendRequest){
        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        FriendshipEntity friendship = friendshipMapper.mapFrom(friendRequest);
        if(!friendshipService.isExists(friendship.getId())){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        friendshipService.delete(friendship.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
