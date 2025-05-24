package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.mappers.Mapper;
import com.pvt.project71.services.security.JwtService;
import com.pvt.project71.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    /**
     * {@code POST /users} - Creates a new user.
     * <p>Expects user data in JSON format containing user information, and valid JWT token.</p>
     *
     * @param user the user data in JSON format. Expected fields:
     *             <ul>
     *             <li><strong>email</strong>: String (required), must follow something@example.com format.</li>
     *             <li><strong>username</strong>: String (optional)</li>
     *             <li><strong>school</strong>: String (optional)</li>
     *             <li><strong>profilePictureUrl</strong>: String (optional)</li>
     *             </ul>
     *             <p><strong>Example JSON:</strong></p>
     *                  <pre>{@code
     *                  {
     *                      "email": "something@example.com,
     *                      "username": "exampleName",
     *                      "school": "exampleSchool",
     *                      "profilePictureUrl": "https://example.com/picture.jpg"
     *                  }
     *                  }</pre>
     * @param userToken the JWT token of the authenticated user.
     *
     * @return ResponseEntity containing:
     *      <ul>
     *          <li>{@code 201 Created} and the user data in JSON format if the user is created.</li>
     *          <li>{@code 401 Unauthorized} if the JWT token is invalid. </li>
     *      <ul>
     */
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

    /**
     * {@code POST /public/users} - Creates a new user without requiring JWT token.
     * <p>Expects user data in JSON format containing user information.</p>
     *
     * @param user the user data in JSON format. Expected fields:
     *             <ul>
     *             <li><strong>email</strong>: String (required), must follow something@example.com format.</li>
     *             <li><strong>username</strong>: String (optional)</li>
     *             <li><strong>school</strong>: String (optional)</li>
     *             <li><strong>profilePictureUrl</strong>: String (optional)</li>
     *             </ul>
     *             <p><strong>Example JSON:</strong></p>
     *                  <pre>{@code
     *                  {
     *                      "email": "something@example.com,
     *                      "username": "exampleName",
     *                      "school": "exampleSchool",
     *                      "profilePictureUrl": "https://example.com/picture.jpg"
     *                  }
     *                  }</pre>
     *
     * @return ResponseEntity containing:
     *      <ul>
     *          <li>{@code 201 Created} and the user data in JSON format.</li>
     *      <ul>
     */
    @PostMapping(path = "/public/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto user, String googleToken){
        UserEntity userEntity = userMapper.mapFrom(user);
        UserEntity savedUserEntity = userService.save(userEntity);
        return new ResponseEntity<>(userMapper.mapTo(savedUserEntity), HttpStatus.CREATED);
    }

    /**
     * {@code GET /users} - Retrieves a list of all registered users.
     * <p> Returns a list of userDto objects, in JSON format, representing each user in the system.</p>
     *
     * @return a ResponseEntity containing a list of userDtos and HTTP status {@code 200 OK}.
     */
    @GetMapping(path = "/users")
    public ResponseEntity<?> listUsers() {
        List<UserEntity> users = userService.findAll();
        List<UserDto> userDtos = users.stream()
                .map(userMapper::mapTo)
                .collect(Collectors.toList());
        return new ResponseEntity<>(userDtos, HttpStatus.OK);
    }

    /**
     * {@code GET /users/{email}} - Retrieves one user by the specified email address.
     * <p> Returns a userDto in JSON format, if the user with specified email is found.</p>
     *
     * @param email the email of the user to retrieve.
     *
     * @return ResponseEntity containing the user data and HTTP status:
     *      <ul>
     *          <li>{@code 200 OK} if the user is found</li>
     *          <li>{@code 404 Not found} if the user with given email is not found.</li>
     *      </ul>
     */
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

    /**
     * {@code PUT /users} - Fully updates an existing users profile using the provided user data provided in the Dto.
     * <p>Expects user data provided in JSON format, and a valid JWT token.
     * Currently only school and username can be updated.</p>
     *
     * @param userDto the user data in JSON format. Expected fields:
     *                  <ul>
     *                  <li><strong>email</strong>: String (required), must follow something@example.com format.</li>
     *                  <li><strong>username</strong>: String (optional)</li>
     *                  <li><strong>school</strong>: String (optional)</li>
     *                  <li><strong>profilePictureUrl</strong>: String (optional)</li>
     *                  </ul>
     *                <p><strong>Example JSON:</strong></p>
     *                       <pre>{@code
     *                      {
     *                          "email": "something@example.com,
     *                          "username": "exampleName",
     *                          "school": "exampleSchool",
     *                          "profilePictureUrl": "https://example.com/picture.jpg"
     *                       }
     *                       }</pre>
     * @param userToken the JWT token of the authenticated user.
     *
     *
     * @return a ResponseEntity containting:
     *  <ul>
     *      <li>HTTP Status {@code 200 OK} and updated user data in JSON format if the update is successful.</li>
     *      <li>HTTP Status {@code 401 Unauthorized} if the JWT token is missing or invalid.</li>
     *      <li>HTTP Status {@code 404 Not Found} if no user is associated with the JWT tokens email.</li>
     *  </ul>
     */
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

    /**
     * {@code PATCH /users} - Partially updates an existing users profile using the provided user data provided in the Dto.
     * <p>Expects user data provided in JSON format, and a valid JWT token.</p>
     *
     * @param userDto the user data in JSON format. Expected fields:
     *                  <ul>
     *                  <li><strong>email</strong>: String (required), must follow something@example.com format.</li>
     *                  <li><strong>username</strong>: String (optional)</li>
     *                  <li><strong>school</strong>: String (optional)</li>
     *                  <li><strong>profilePictureUrl</strong>: String (optional)</li>
     *                  </ul>
     *                <p><strong>Example JSON:</strong></p>
     *                    <pre>{@code
     *                      {
     *                         "email": "something@example.com,
     *                         "username": "exampleName",
     *                         "school": "exampleSchool",
     *                         "profilePictureUrl": "https://example.com/picture.jpg"
     *                      }
     *                   }</pre>
     * @param userToken the JWT token of the authenticated user.
     *
     *
     * @return a ResponseEntity containting:
     *  <ul>
     *      <li>HTTP Status {@code 200 OK} and updated user data in JSON format if the update is successful.</li>
     *      <li>HTTP Status {@code 401 Unauthorized} if the JWT token is missing or invalid.</li>
     *      <li>HTTP Status {@code 404 Not Found} if no user is associated with the JWT tokens email.</li>
     *  </ul>
     */
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
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        userDto.setEmail(emailFromToken);
        UserEntity userEntity = userMapper.mapFrom(userDto);
        UserEntity updatedUser = userService.partialUpdate(emailFromToken, userEntity);
        return new ResponseEntity<>(userMapper.mapTo(updatedUser), HttpStatus.OK);
    }

    /**
     * {@code DELETE /users} – Deletes the user associated with the JWT token.
     *
     * <p>Expects a valid JWT token identifying the user to be deleted.</p>
     *
     * @param userToken the JWT token of the authenticated user.
     *
     * @return a ResponseEntity containing:
     * <ul>
     *   <li>{@code 204 No Content} if the user was successfully deleted.</li>
     *   <li>{@code 401 Unauthorized} if the token is missing, invalid, or if the user does not exist.</li>
     * </ul>
     */
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
