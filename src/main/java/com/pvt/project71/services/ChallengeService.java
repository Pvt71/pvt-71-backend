package com.pvt.project71.services;

import com.pvt.project71.domain.dto.ChallengeDto;
import com.pvt.project71.domain.entities.ChallengeEntity;

public interface ChallengeService {
    ChallengeEntity createChallenge(ChallengeEntity challengeEntity);
}
