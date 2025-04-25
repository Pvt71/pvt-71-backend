package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.ChallengeAttemptDto;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.ChallengeAttemptEntity;
import com.pvt.project71.domain.entities.ChallengeAttemptId;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.mappers.mapperimpl.ChallengeAttemptMapper;
import com.pvt.project71.services.ChallengeAttemptService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/challenges/{id}")
public class ChallengeAttemptController {
    private ChallengeAttemptService challengeAttemptService;
    private ChallengeAttemptMapper challengeAttemptMapper;


    public ChallengeAttemptController(ChallengeAttemptService challengeAttemptService, ChallengeAttemptMapper challengeAttemptMapper) {
        this.challengeAttemptService = challengeAttemptService;
        this.challengeAttemptMapper = challengeAttemptMapper;
    }

    @PostMapping("/submit/{content}")
    public ResponseEntity<ChallengeAttemptDto> submitChallengeAttempt(@PathVariable("id") Integer id, @PathVariable("content") String content
            , @AuthenticationPrincipal UserDto userDto) {
        //För nu skapar vi en test user bara,TODO: ska tas bort när Authentication funkar korrekt
        userDto = UserDto.builder().email("Test@Test.com").school("University").username("Tester").build();
        ChallengeAttemptEntity challengeAttemptEntity = ChallengeAttemptEntity.builder()
                .id(new ChallengeAttemptId(id, userDto.getEmail())).submittedAt(LocalDateTime.now()).content(content)
                .build();
        try {
            return new ResponseEntity<>(challengeAttemptMapper.mapTo(challengeAttemptService.submit(challengeAttemptEntity)), HttpStatus.CREATED);
        } catch (NoSuchElementException noSuchElementException) {
            return new ResponseEntity<ChallengeAttemptDto>(HttpStatus.NOT_FOUND);
        } catch (DuplicateKeyException duplicateKeyException) {
            return new ResponseEntity<ChallengeAttemptDto>(HttpStatus.CONFLICT);
        }
    }
    @PatchMapping("/accept/{userEmail}")
    public ResponseEntity<ChallengeAttemptDto> acceptChallengeAttempt( @PathVariable("id") Integer id, @PathVariable("userEmail") String email) {
        Optional<ChallengeAttemptEntity> attemptToAccept = challengeAttemptService.find(new ChallengeAttemptId(id, email));
        if (attemptToAccept.isEmpty()) {
            return new ResponseEntity<ChallengeAttemptDto>(HttpStatus.NOT_FOUND);
        } else if (attemptToAccept.get().getStatus() == Status.ACCEPTED) {
            return new ResponseEntity<ChallengeAttemptDto>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(challengeAttemptMapper.mapTo(challengeAttemptService.accept(attemptToAccept.get())), HttpStatus.OK);
    }

}
