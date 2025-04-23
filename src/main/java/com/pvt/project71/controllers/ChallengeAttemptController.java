package com.pvt.project71.controllers;

import com.pvt.project71.mappers.mapperimpl.ChallengeAttemptMapper;
import com.pvt.project71.services.ChallengeAttemptService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChallengeAttemptController {
    private ChallengeAttemptService challengeAttemptService;
    private ChallengeAttemptMapper challengeAttemptMapper;


    public ChallengeAttemptController(ChallengeAttemptService challengeAttemptService, ChallengeAttemptMapper challengeAttemptMapper) {
        this.challengeAttemptService = challengeAttemptService;
        this.challengeAttemptMapper = challengeAttemptMapper;
    }
}
