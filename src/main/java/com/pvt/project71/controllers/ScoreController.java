package com.pvt.project71.controllers;


import com.pvt.project71.domain.dto.ScoreDto;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.domain.entities.score.ScoreId;
import com.pvt.project71.mappers.mapperimpl.ScoreMapper;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.security.JwtService;
import com.pvt.project71.services.ScoreService;
import com.pvt.project71.services.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class ScoreController {
    private final ScoreService scoreService;
    private final JwtService jwtService;
    private final UserService userService;
    private final EventService eventService;
    private final ScoreMapper scoreMapper;

    public ScoreController(ScoreService scoreService, JwtService jwtService, UserService userService, EventService eventService, ScoreMapper scoreMapper) {
        this.scoreService = scoreService;
        this.jwtService = jwtService;
        this.userService = userService;
        this.eventService = eventService;
        this.scoreMapper = scoreMapper;
    }

    //TODO: Add functionality to fetch by event and remove by event after discussing with group


    @PostMapping("/events/{eventId}/join")
    public ResponseEntity<ScoreDto> createScore(@PathVariable("eventId") Integer eventId, @AuthenticationPrincipal Jwt userToken) {
        if (!jwtService.isTokenValid(userToken)) {
            return  new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            return  new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } if (!eventService.isExists(eventId)) {
            return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ScoreId id = new ScoreId(user.get(), eventService.findOne(eventId).get());
        if (scoreService.findOne(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        ScoreEntity scoredOpt = scoreService.create(ScoreEntity.builder().scoreId(id).build());
        return  new ResponseEntity<>(scoreMapper.mapTo(scoredOpt), HttpStatus.CREATED);
    }
    //NOTE: The Dto diagram shows that there is no need for a read many
    //as (user,event) maps to one score.
    @GetMapping("/scores/{email}/{eventId}")
    public ResponseEntity<ScoreDto> getScore(@NotBlank @Email @PathVariable String email, @PathVariable int eventId) {
        //Feels a bit odd to do thias as ScoreService#findOne checks by email and eventid
        ScoreId scoreId = ScoreId.builder().user(userService.findOne(email).get())
                .event(eventService.findOne(eventId).get()).build();
        Optional<ScoreEntity> scoreOpt = scoreService.findOne(scoreId);
        if (scoreOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(scoreMapper.mapTo(scoreOpt.get()),HttpStatus.OK);
    }
    //All scores that belong to a user
    @GetMapping("/scores/users/{email}")
    public ResponseEntity<List<ScoreDto>> getAllUserScores(@NotBlank @Email @PathVariable String email) {
        Optional<List<ScoreEntity>> scoreOpt = scoreService.findAllByUser(userService.findOne(email).get());
        if (scoreOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        List<ScoreDto> scores = scoreOpt.get().stream().map(scoreMapper::mapTo).toList();
        return new ResponseEntity<>(scores,HttpStatus.OK);
    }
    //Get ALL scores related to an event
    @GetMapping("/scores/events/{eventId}")
    public ResponseEntity<List<ScoreDto>> getAllUserScores(@PathVariable int eventId) {
        Optional<List<ScoreEntity>> scoreOpt = scoreService.findAllByEvent(eventId);
        if (scoreOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        List<ScoreDto> scores = scoreOpt.get().stream().map(scoreMapper::mapTo).toList();
        return new ResponseEntity<>(scores,HttpStatus.OK);
    }
    @GetMapping("/scores/mySchool")
    public ResponseEntity<List<ScoreDto>> getAllScoresForOwnSchool(@AuthenticationPrincipal Jwt userToken) {
        if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<ScoreEntity> scoreEntities = scoreService.findAllByEvent(eventService.getDefaultEvent(user.get().getSchool())
                .getId()).get();
        List<ScoreDto> scores = scoreEntities.stream().map(scoreMapper::mapTo).toList();
        return new ResponseEntity<>(scores, HttpStatus.OK);
    }
    @GetMapping("/scores/university/{universityName}")
    public ResponseEntity<List<ScoreDto>> getAllScoresForSchool(@PathVariable("universityName") String universityName) {
        Optional<EventEntity> schoolEvent = eventService.findOneByName(universityName);
        if (schoolEvent.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<List<ScoreEntity>> scoreEntities = scoreService.findAllByEvent(schoolEvent.get().getId());
        if (scoreEntities.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<ScoreDto> scores = scoreEntities.get().stream().map(scoreMapper::mapTo).toList();
        return new ResponseEntity<>(scores, HttpStatus.OK);
    }


}
