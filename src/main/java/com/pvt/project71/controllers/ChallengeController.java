package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.ChallengeDto;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.mappers.Mapper;
import com.pvt.project71.services.ChallengeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
