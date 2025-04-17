package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.ChallengeDto;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.mappers.Mapper;
import com.pvt.project71.services.ChallengeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class ChallengeController {

    private ChallengeService challengeService;
    private Mapper<ChallengeEntity, ChallengeDto> challengeMapper;

    public ChallengeController(ChallengeService challengeService,Mapper<ChallengeEntity, ChallengeDto> challengeMapper) {
        this.challengeService = challengeService;
        this.challengeMapper = challengeMapper;
    }

    @PostMapping(path = "/challenges")
    public ChallengeDto createChallenge(@RequestBody ChallengeDto challengeDto) {
        ChallengeEntity savedChallengeEntity = challengeService.createChallenge(challengeMapper.mapFrom(challengeDto));
        return challengeMapper.mapTo(savedChallengeEntity);
    }
    @GetMapping(path = "/challenges/{id}")
    public ResponseEntity<ChallengeDto> getChallenge(@PathVariable("id") Integer id) {
        Optional<ChallengeEntity> found = challengeService.find(id);
        return found.map(challengeEntity -> {
            ChallengeDto dto = challengeMapper.mapTo(challengeEntity);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @DeleteMapping(path = "/challenges/{id}")
    public ResponseEntity deleteChallenge(@PathVariable("id") Integer id) {
        challengeService.delete(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
