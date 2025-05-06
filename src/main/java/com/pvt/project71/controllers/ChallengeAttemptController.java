package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.ChallengeAttemptDto;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.ChallengeAttemptEntity;
import com.pvt.project71.domain.entities.ChallengeAttemptId;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.mappers.mapperimpl.ChallengeAttemptMapper;
import com.pvt.project71.services.ChallengeAttemptService;
import com.pvt.project71.services.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/challenges/{id}")
public class ChallengeAttemptController {
    private ChallengeAttemptService challengeAttemptService;
    private ChallengeAttemptMapper challengeAttemptMapper;
    private UserService userService;


    public ChallengeAttemptController(ChallengeAttemptService challengeAttemptService, ChallengeAttemptMapper challengeAttemptMapper, UserService userService) {
        this.challengeAttemptService = challengeAttemptService;
        this.challengeAttemptMapper = challengeAttemptMapper;
        this.userService = userService;
    }

    @PostMapping("/submit/{content}")
    public ResponseEntity<ChallengeAttemptDto> submitChallengeAttempt(@PathVariable("id") Integer id, @PathVariable("content") String content
            , @AuthenticationPrincipal Jwt userToken) {
        if (userToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No token found");
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
    @PatchMapping("/accept/{userEmail}")
    public ResponseEntity<ChallengeAttemptDto> acceptChallengeAttempt( @PathVariable("id") Integer id, @PathVariable("userEmail") String email,
                                                                       @AuthenticationPrincipal Jwt userToken) {
        if (userToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No token found");
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

}
