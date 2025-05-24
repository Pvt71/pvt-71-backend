package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.ChallengeAttemptDto;
import com.pvt.project71.domain.entities.ChallengeAttemptEntity;
import com.pvt.project71.domain.entities.ChallengeAttemptId;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.enums.ProofType;
import com.pvt.project71.mappers.mapperimpl.ChallengeAttemptMapper;
import com.pvt.project71.services.ChallengeAttemptService;
import com.pvt.project71.services.security.JwtService;
import com.pvt.project71.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

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
        UserEntity user = checkAndRetrieveUserFromToken(userToken);

        ChallengeAttemptEntity challengeAttemptEntity = ChallengeAttemptEntity.builder()
                .id(new ChallengeAttemptId(id, user.getEmail())).submittedAt(LocalDateTime.now()).content(content).username(user.getUsername())
                .build();
        return new ResponseEntity<>(challengeAttemptMapper.mapTo(challengeAttemptService.submit(challengeAttemptEntity)), HttpStatus.CREATED);
    }
    @PostMapping("/challenges/{id}/submit")
    public ResponseEntity<ChallengeAttemptDto> submitChallengeAttemptWithoutContet(@PathVariable("id") Integer id
            , @AuthenticationPrincipal Jwt userToken) {
        UserEntity user = checkAndRetrieveUserFromToken(userToken);

        ChallengeAttemptEntity challengeAttemptEntity = ChallengeAttemptEntity.builder()
                .id(new ChallengeAttemptId(id, user.getEmail())).submittedAt(LocalDateTime.now()).content("").username(user.getUsername())
                .build();
        return new ResponseEntity<>(challengeAttemptMapper.mapTo(challengeAttemptService.submit(challengeAttemptEntity)), HttpStatus.CREATED);
    }

    @PostMapping("/challenges/{id}/sync/{content}")
    public ResponseEntity<ChallengeAttemptDto> syncWithPairContent(@PathVariable("id") Integer id, @PathVariable("content") String content
            , @AuthenticationPrincipal Jwt userToken) {
        UserEntity user = checkAndRetrieveUserFromToken(userToken);

        ChallengeAttemptEntity challengeAttemptEntity = ChallengeAttemptEntity.builder()
                .id(new ChallengeAttemptId(id, user.getEmail())).submittedAt(LocalDateTime.now()).content(content)
                .build();
        if (challengeAttemptEntity.getChallenge().getProofType() != ProofType.PAIR_CONTENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge doesnt do Pair Content as proof");
        }
        return new ResponseEntity<>(challengeAttemptMapper.mapTo(challengeAttemptService.sync(challengeAttemptEntity)), HttpStatus.CREATED);
    }

    @PatchMapping("/challenges/{id}/accept/{userEmail}")
    public ResponseEntity<ChallengeAttemptDto> acceptChallengeAttempt( @PathVariable("id") Integer id, @PathVariable("userEmail") String email,
                                                                       @AuthenticationPrincipal Jwt userToken) {
        UserEntity user = checkAndRetrieveUserFromToken(userToken);

        Optional<ChallengeAttemptEntity> attemptToAccept = challengeAttemptService.find(new ChallengeAttemptId(id, email));
        if (attemptToAccept.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt doesnt exist");
        }
        return new ResponseEntity<>(challengeAttemptMapper.mapTo(challengeAttemptService.accept(attemptToAccept.get(), user)), HttpStatus.OK);
    }

    @PatchMapping("/challenges/{id}/reject/{userEmail}")
    public ResponseEntity<ChallengeAttemptDto> rejectChallengeAttempt(@PathVariable("id") Integer id, @PathVariable("userEmail") String email,
                                                                        @AuthenticationPrincipal Jwt userToken) {
        UserEntity user = checkAndRetrieveUserFromToken(userToken);
        Optional<ChallengeAttemptEntity> attemptToReject = challengeAttemptService.find(new ChallengeAttemptId(id, email));
        if (attemptToReject.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt doesnt exist");
        }
        return  new ResponseEntity<>(challengeAttemptMapper.mapTo(challengeAttemptService.reject(attemptToReject.get(), user)), HttpStatus.OK);
    }

    @GetMapping("/challenges/pending")
    public ResponseEntity<List<ChallengeAttemptDto>> findAllPendingAcceptableByUser(@AuthenticationPrincipal Jwt userToken) {
        UserEntity user = checkAndRetrieveUserFromToken(userToken);

        List<ChallengeAttemptDto> dtos = challengeAttemptService.getAttemptsUserCanAllow(user).stream()
                .map(challengeAttemptMapper::mapTo)
                .collect(toList());
        if (dtos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    /**
     *
     * @param userToken är okej om det inte är rätt, om token har samma mail som frågar får man full info dvs content
     */
    @GetMapping("/challenges/attempts/{email}")
    public ResponseEntity<List<ChallengeAttemptDto>> findAllExistingAttemptsByUserEmail(@PathVariable("email") String email,
                                                                                        @AuthenticationPrincipal Jwt userToken) {
        List<ChallengeAttemptDto> dtos = challengeAttemptService.getAttemptsSubmittedByUser(email).stream()
                .map(challengeAttemptMapper::mapTo)
                .toList();
        if (dtos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!jwtService.isTokenValid(userToken) || !userToken.getSubject().equals(email)) {
            for (ChallengeAttemptDto c : dtos) {
                c.setContent("");
            }
        }
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    private UserEntity checkAndRetrieveUserFromToken(Jwt userToken) {
        if (!jwtService.isTokenValid(userToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is not valid");
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token could not be linked to an user");
        }
        return user.get();
    }
}
