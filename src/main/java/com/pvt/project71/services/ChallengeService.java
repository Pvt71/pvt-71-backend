package com.pvt.project71.services;

import com.pvt.project71.domain.dto.ChallengeDto;
import com.pvt.project71.domain.entities.ChallengeEntity;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface ChallengeService {
    ChallengeEntity save(ChallengeEntity challengeEntity) throws NoSuchElementException;

    Optional<ChallengeEntity> find(Integer id);

    void delete(Integer id);

    ChallengeEntity partialUpdate(Integer id, ChallengeEntity challengeEntity);

    List<ChallengeEntity> getChallenges(String email, Integer eventId);
}
