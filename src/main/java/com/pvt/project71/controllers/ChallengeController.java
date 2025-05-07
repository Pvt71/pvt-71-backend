package com.pvt.project71.controllers;

import com.pvt.project71.domain.TimeStamps;
import com.pvt.project71.domain.dto.ChallengeDto;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.mappers.Mapper;
import com.pvt.project71.services.ChallengeService;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private UserService userService;
    private JwtService jwtService;

    public ChallengeController(ChallengeService challengeService, Mapper<ChallengeEntity, ChallengeDto> challengeMapper, UserService userService, JwtService jwtService) {
        this.challengeService = challengeService;
        this.challengeMapper = challengeMapper;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping(path = "/challenges")
    public ResponseEntity<ChallengeDto> createChallenge(@Valid @RequestBody ChallengeDto challengeDto, @AuthenticationPrincipal Jwt userToken) {
        if (challengeDto.getDates() == null || challengeDto.getDates().getEndsAt() == null ||
                (challengeDto.getDates().getStartsAt() != null && challengeDto.getDates().getStartsAt().isBefore(LocalDateTime.now()))) {
            return new ResponseEntity<ChallengeDto>(HttpStatus.BAD_REQUEST);
        } if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        challengeDto.getDates().setCreatedAt(null);
        ChallengeEntity challengeEntity = challengeMapper.mapFrom(challengeDto);
        challengeEntity.setCreator(user.get());
        challengeEntity = challengeService.save(challengeEntity, user.get());
        return new ResponseEntity<>(challengeMapper.mapTo(challengeEntity), HttpStatus.CREATED);
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
    public ResponseEntity deleteChallenge(@PathVariable("id") Integer id, @AuthenticationPrincipal Jwt userToken) {
        if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (challengeService.find(id).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        challengeService.delete(id, user.get());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping(path = "/challenges/{id}")
    public ResponseEntity<ChallengeDto> partialUpdate(
            @PathVariable("id") Integer id,
            @RequestBody ChallengeDto challengeDto,
            @AuthenticationPrincipal Jwt userToken) {
        if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        Optional<ChallengeEntity> found = challengeService.find(id);
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (found.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        challengeDto.setId(id);
        ChallengeEntity challengeEntity = challengeMapper.mapFrom(challengeDto);
        challengeEntity.setCreator(found.get().getCreator());
        challengeEntity.setEvent(found.get().getEvent());
        ChallengeEntity updatedChallenge = challengeService.partialUpdate(id, challengeEntity, user.get());
        return new ResponseEntity<>(challengeMapper.mapTo(updatedChallenge), HttpStatus.OK);
    }

    @PutMapping(path = "/challenges/{id}")
    public ResponseEntity<ChallengeDto> fullUpdate(@PathVariable("id") Integer id, @RequestBody ChallengeDto challengeDto,
                                                   @AuthenticationPrincipal Jwt userToken) {
        Optional<ChallengeEntity> found = challengeService.find(id);
        if (found.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        challengeDto.setDates(found.get().getDates());
        challengeDto.getDates().setUpdatedAt(LocalDateTime.now());
        challengeDto.setId(id);
        ChallengeEntity challengeEntity = challengeMapper.mapFrom(challengeDto);
        challengeEntity.setEvent(found.get().getEvent());
        ChallengeEntity updatedChallenge = challengeService.save(challengeEntity, user.get());
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
