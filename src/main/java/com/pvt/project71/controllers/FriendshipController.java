package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.FriendshipDto;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.FriendshipEntity;
import com.pvt.project71.domain.entities.FriendshipId;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.mappers.Mapper;
import com.pvt.project71.services.FriendshipService;
import com.pvt.project71.services.security.JwtService;
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

    /**
     * {@code POST /friends/add/{email}} - Sends a new friend request.
     * <p>Expects an email to the user you want to add, and valid JWT token.</p>
     *
     * @param receiverEmail the email of the user you want to send the request to
     * @param requesterToken the JWT token of the authenticated user.
     *
     * @return ResponseEntity containing:
     *      <ul>
     *          <li>{@code 201 Created} and the friendship data in JSON format if the friend request is created.</li>
     *          <li>{@code 400 Bad Request} if the users are already friends, or if you try to add yourself. </li>
     *          <li>{@code 401 Unauthorized} if the JWT token is invalid. </li>
     *          <li>{@code 404 Not Found} if the requester or receiver does not exist. </li>
     *      <ul>
     */
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

    /**
     * {@code PUT /friends/accept/{email}} - Accepts a friend request.
     * <p>Expects an email to the user who sent the request to you, and valid JWT token.</p>
     *
     * @param requesterEmail the email of the user who sent the request to you.
     * @param receiverToken the JWT token of the authenticated user.
     *
     * @return ResponseEntity containing:
     *      <ul>
     *          <li>{@code 200 OK} and the friendship data in JSON format if the friend request is accepted.</li>
     *          <li>{@code 400 Bad Request} if the user is receiver and requester, or if the friendship is not pending. </li>
     *          <li>{@code 401 Unauthorized} if the JWT token is invalid. </li>
     *          <li>{@code 404 Not Found} if the friend request is not found. </li>
     *      <ul>
     */
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

    /**
     * {@code GET /friendrequests} - Returns a list of userDto in JSON format who has sent requests to the user.
     * <p>Expects a valid JWT token.</p>
     *
     * @param userToken the JWT token of the authenticated user.
     *
     * @return ResponseEntity containing:
     *      <ul>
     *          <li>{@code 200 OK} and a list of UserDtos in JSON format.</li>
     *          <li>{@code 401 Unauthorized} if the JWT token is invalid. </li>
     *      <ul>
     */
    @GetMapping(path = "/friendrequests")
    public ResponseEntity<List<UserDto>> getFriendRequests(@AuthenticationPrincipal Jwt userToken){
        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<UserDto> friends = friendshipService.findAllByStatus(userToken.getSubject(), Status.PENDING);
        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    /**
     * {@code GET /friends} - Returns a list of userDto in JSON format who are friends with the user.
     * <p>Expects a valid JWT token.</p>
     *
     * @param userToken the JWT token of the authenticated user.
     *
     * @return ResponseEntity containing:
     *      <ul>
     *          <li>{@code 200 OK} and a list of UserDtos in JSON format.</li>
     *          <li>{@code 401 Unauthorized} if the JWT token is invalid. </li>
     *      <ul>
     */
    @GetMapping(path = "/friends")
    public ResponseEntity<List<UserDto>> getFriends(@AuthenticationPrincipal Jwt userToken){
        if(userToken == null || !jwtService.isTokenValid(userToken)){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<UserDto> friends = friendshipService.findAllByStatus(userToken.getSubject(), Status.ACCEPTED);

        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    /**
     * {@code DELETE /friends/{email}} - Used for both deleting friendships and rejecting friend requests.
     * <p>Expects an email to the other user, and a valid JWT token.</p>
     *
     * @param otherUsersEmail the email of the other user.
     * @param userToken the JWT token of the authenticated user.
     *
     * @return ResponseEntity containing:
     *      <ul>
     *          <li>{@code 204 No Content} if the friendship is deleted.</li>
     *          <li>{@code 401 Unauthorized} if the JWT token is invalid. </li>
     *          <li>{@code 404 Not Found} if the friendship or friend request does not exist.</li>
     *      <ul>
     */
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
