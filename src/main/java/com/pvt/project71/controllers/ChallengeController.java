package com.pvt.project71.controllers;

import com.pvt.project71.domain.TimeStamps;
import com.pvt.project71.domain.dto.ChallengeDto;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.mappers.Mapper;
import com.pvt.project71.services.ChallengeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class ChallengeController {

    private ChallengeService challengeService;
    private Mapper<ChallengeEntity, ChallengeDto> challengeMapper;

    public ChallengeController(ChallengeService challengeService,Mapper<ChallengeEntity, ChallengeDto> challengeMapper) {
        this.challengeService = challengeService;
        this.challengeMapper = challengeMapper;
    }

    @PostMapping(path = "/challenges")
    public ResponseEntity<ChallengeDto> createChallenge(@Valid @RequestBody ChallengeDto challengeDto) {
        if (challengeDto.getDates() == null) {
            return new ResponseEntity<ChallengeDto>(HttpStatus.BAD_REQUEST);
        } if (challengeDto.getDates().getEndsAt() == null) {
            return new ResponseEntity<ChallengeDto>(HttpStatus.BAD_REQUEST);
        }
        if (challengeDto.getDates().getStartsAt() != null && challengeDto.getDates().getStartsAt().isBefore(LocalDateTime.now())) {
            return new ResponseEntity<ChallengeDto>(HttpStatus.BAD_REQUEST); //Om start börjar innan nu
        }

        ChallengeEntity savedChallengeEntity = challengeService.save(challengeMapper.mapFrom(challengeDto));
        return new ResponseEntity<>(challengeMapper.mapTo(savedChallengeEntity), HttpStatus.CREATED);
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
        if (challengeService.find(id).isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        challengeService.delete(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PatchMapping(path = "/challenges/{id}")
    public ResponseEntity<ChallengeDto> partialUpdate(@PathVariable("id") Integer id, @RequestBody ChallengeDto challengeDto) {
        Optional<ChallengeEntity> found = challengeService.find(id);
        if (found.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        challengeDto.setId(id);
        ChallengeEntity challengeEntity = challengeMapper.mapFrom(challengeDto);
        challengeEntity.setEvent(found.get().getEvent());
        ChallengeEntity updatedChallenge = challengeService.partialUpdate(id, challengeEntity);
        return new ResponseEntity<>(challengeMapper.mapTo(updatedChallenge), HttpStatus.OK);
    }

    @PutMapping(path = "/challenges/{id}")
    public ResponseEntity<ChallengeDto> fullUpdate(@PathVariable("id") Integer id, @RequestBody ChallengeDto challengeDto) {
        Optional<ChallengeEntity> found = challengeService.find(id);
        if (found.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        challengeDto.setDates(found.get().getDates());
        challengeDto.getDates().setUpdatedAt(LocalDateTime.now());
        challengeDto.setId(id);
        ChallengeEntity challengeEntity = challengeMapper.mapFrom(challengeDto);
        challengeEntity.setEvent(found.get().getEvent());
        ChallengeEntity updatedChallenge = challengeService.save(challengeEntity);
        return  new ResponseEntity<>(challengeMapper.mapTo(updatedChallenge), HttpStatus.OK);
    }

    @GetMapping("/challenges")
    public ResponseEntity<List<ChallengeDto>> getChallenges(@RequestParam(value = "user", required = false) String email,
                                                                          @RequestParam(value = "eventId", required = false) Integer eventId) {
        List<ChallengeEntity> challenges = challengeService.getChallenges(email, eventId);
        List<ChallengeDto> dtos = challenges.stream()
                .map(challengeMapper::mapTo)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
