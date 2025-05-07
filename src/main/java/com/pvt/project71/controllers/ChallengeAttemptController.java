package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.ChallengeAttemptDto;
import com.pvt.project71.domain.dto.ChallengeDto;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.ChallengeAttemptEntity;
import com.pvt.project71.domain.entities.ChallengeAttemptId;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.mappers.mapperimpl.ChallengeAttemptMapper;
import com.pvt.project71.services.ChallengeAttemptService;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class ChallengeAttemptController {
    private ChallengeAttemptService challengeAttemptService;
    private ChallengeAttemptMapper challengeAttemptMapper;
    private UserService userService;
    private JwtService jwtService;


    public ChallengeAttemptController(ChallengeAttemptService challengeAttemptService, ChallengeAttemptMapper challengeAttemptMapper, UserService userService, JwtService jwtService) {
        this.challengeAttemptService = challengeAttemptService;
        this.challengeAttemptMapper = challengeAttemptMapper;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/challenges/{id}/submit/{content}")
    public ResponseEntity<ChallengeAttemptDto> submitChallengeAttempt(@PathVariable("id") Integer id, @PathVariable("content") String content
            , @AuthenticationPrincipal Jwt userToken) {
        if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token could not be linked to an user");
        }
        ChallengeAttemptEntity challengeAttemptEntity = ChallengeAttemptEntity.builder()
                .id(new ChallengeAttemptId(id, user.get().getEmail())).submittedAt(LocalDateTime.now()).content(content)
                .build();
        return new ResponseEntity<>(challengeAttemptMapper.mapTo(challengeAttemptService.submit(challengeAttemptEntity)), HttpStatus.CREATED);
    }
    @PatchMapping("/challenges/{id}/accept/{userEmail}")
    public ResponseEntity<ChallengeAttemptDto> acceptChallengeAttempt( @PathVariable("id") Integer id, @PathVariable("userEmail") String email,
                                                                       @AuthenticationPrincipal Jwt userToken) {
        if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token could not be linked to an user");
        }
        Optional<ChallengeAttemptEntity> attemptToAccept = challengeAttemptService.find(new ChallengeAttemptId(id, email));
        if (attemptToAccept.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt doesnt exist");
        }
        return new ResponseEntity<>(challengeAttemptMapper.mapTo(challengeAttemptService.accept(attemptToAccept.get(), user.get())), HttpStatus.OK);
    }
    @GetMapping("/challenges/pending")
    public ResponseEntity<List<ChallengeAttemptDto>> findAllPendingAcceptableByUser(@AuthenticationPrincipal Jwt userToken) {
        if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token could not be linked to an user");
        }
        List<ChallengeAttemptDto> dtos = challengeAttemptService.getAttemptsUserIsPermittedToAllow(user.get()).stream()
                .map(challengeAttemptMapper::mapTo)
                .collect(Collectors.toList());
        if (dtos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

}
