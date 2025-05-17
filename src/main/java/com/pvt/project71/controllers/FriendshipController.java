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

    @PostMapping(path = "/friends/add/{email}")
    public ResponseEntity<FriendshipDto> sendFriendRequest(@PathVariable("email") String receiverEmail,
                                                           @AuthenticationPrincipal Jwt requesterToken){
        if(requesterToken == null || !jwtService.isTokenValid(requesterToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        //Can't send request to already existing friendship
        if(friendshipService.isExists(requesterToken.getSubject(), receiverEmail)){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Both users must exist
        if(!userService.isExists(receiverEmail) || !userService.isExists(requesterToken.getSubject())){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Can't add yourself
        if(receiverEmail.equals(requesterToken.getSubject())){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<UserEntity> requester = userService.findOne(requesterToken.getSubject());
        Optional<UserEntity> accepter = userService.findOne(receiverEmail);

        FriendshipEntity friendship = FriendshipEntity.builder()
                        .id(new FriendshipId(requesterToken.getSubject(), receiverEmail))
                        .requester(requester.get())
                        .receiver(accepter.get())
                        .status(Status.PENDING)
                        .build();

        FriendshipEntity savedFriendship = friendshipService.save(friendship);
        return new ResponseEntity<>(friendshipMapper.mapTo(savedFriendship), HttpStatus.CREATED);
    }

    @PutMapping(path = "/friends/accept/{email}")
    public ResponseEntity<FriendshipDto> acceptFriendRequest(@PathVariable("email") String requesterEmail,
                                                             @AuthenticationPrincipal Jwt receiverToken){
        if(receiverToken == null || !jwtService.isTokenValid(receiverToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        FriendshipEntity friendRequest = friendshipService.findSpecificFriendRequest(requesterEmail);
        if(friendRequest == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //User can't accept requests the user sent, friendships must be pending
        if(receiverToken.getSubject().equals(friendRequest.getRequester().getEmail()) || friendRequest.getStatus() != Status.PENDING){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        friendRequest.setStatus(Status.ACCEPTED);
        friendRequest.setFriendsSince(LocalDate.now());
        return new ResponseEntity<>(friendshipMapper.mapTo(friendshipService.save(friendRequest)), HttpStatus.OK);
    }

    @GetMapping(path = "/friendrequests")
    public ResponseEntity<List<UserDto>> getFriendRequests(@AuthenticationPrincipal Jwt userToken){
        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<UserDto> friends = friendshipService.findAllByStatus(userToken.getSubject(), Status.PENDING);
        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    @GetMapping(path = "/friends")
    public ResponseEntity<List<UserDto>> getFriends(@AuthenticationPrincipal Jwt userToken){
        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<UserDto> friends = friendshipService.findAllByStatus(userToken.getSubject(), Status.ACCEPTED);

        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    // Reject requests and remove friendships
    @DeleteMapping(path = "/friends/{email}")
    public ResponseEntity<FriendshipDto> deleteFriendship(@PathVariable("email") String otherUsersEmail,
                                                          @AuthenticationPrincipal Jwt userToken){
        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        FriendshipEntity friendship = friendshipService.findFriendship(userToken.getSubject(), otherUsersEmail);
        if(friendship == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        friendshipService.delete(friendship.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
