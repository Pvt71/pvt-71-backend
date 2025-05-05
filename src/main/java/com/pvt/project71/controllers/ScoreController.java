package com.pvt.project71.controllers;


import com.pvt.project71.domain.dto.ScoreDto;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.domain.entities.score.ScoreId;
import com.pvt.project71.mappers.mapperimpl.ScoreMapper;
import com.pvt.project71.services.JWTService;
import com.pvt.project71.services.ScoreService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class ScoreController {
    private final ScoreService scoreService;
    private final JWTService jwtService;
    private final ScoreMapper scoreMapper;
    public ScoreController(ScoreService scoreService, JWTService jwtService, ScoreMapper scoreMapper) {
        this.scoreService = scoreService;
        this.jwtService = jwtService;
        this.scoreMapper = scoreMapper;
    }

    //TODO: Add functionality to fetch by event and remove by event after discussing with group


    @PostMapping("/scores")
    public ResponseEntity<ScoreDto> createScore(@Valid  @RequestBody ScoreDto scoreDto) {
        Optional<ScoreEntity> scoredOpt = scoreService.create(scoreDto);
        //If e-mail does not belong to an user or if there is no event with provided event id
        if (scoredOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return  new ResponseEntity<>(scoreMapper.mapTo(scoredOpt.get()), HttpStatus.CREATED);
    }
    //NOTE: The Dto diagram shows that there is no need for a read many
    //as (user,event) maps to one score.
    @GetMapping("/scores/{email}/{eventId}")
    public ResponseEntity<ScoreDto> getScore(@NotBlank @Email @PathVariable String email, @PathVariable int eventId) {
        Optional<ScoreEntity> scoreOpt = scoreService.findOne(email,eventId);
        if (scoreOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(scoreMapper.mapTo(scoreOpt.get()),HttpStatus.OK);
    }
    //All scores that belong to a user
    @GetMapping("/scores/users/r{email}")
    public ResponseEntity<List<ScoreDto>> getAllUserScores(@NotBlank @Email @PathVariable String email) {
        Optional<List<ScoreEntity>> scoreOpt = scoreService.findAllByUser(email);
        if (scoreOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        List<ScoreDto> scores = scoreOpt.get().stream().map(scoreMapper::mapTo).toList();
        return new ResponseEntity<>(scores,HttpStatus.OK);
    }
    @GetMapping("/scores/events/{eventId}")
    public ResponseEntity<List<ScoreDto>> getAllEventScores(@PathVariable int  eventId) {
        Optional<List<ScoreEntity>> scoreOpt = scoreService.findAllByEvent(eventId);
        if (scoreOpt.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        List<ScoreDto> scores = scoreOpt.get().stream().map(scoreMapper::mapTo).toList();
        return new ResponseEntity<>(scores,HttpStatus.OK);
    }

    //delete provided email and event
    @DeleteMapping("/scores/{email}/{eventId}")
    public ResponseEntity deleteScore(@NotBlank @Email @PathVariable String email, @PathVariable int eventId) {
        scoreService.delete(email,eventId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
    @DeleteMapping("/scores")
    public ResponseEntity deleteScore(@Valid @RequestBody ScoreDto scoreDto) {
        scoreService.delete(scoreMapper.mapFrom(scoreDto));
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
