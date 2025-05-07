package com.pvt.project71.services;

import com.pvt.project71.domain.dto.ChallengeDto;
import com.pvt.project71.domain.entities.ChallengeEntity;
import org.springframework.transaction.annotation.Transactional;
import com.pvt.project71.domain.entities.UserEntity;

import java.util.List;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface ChallengeService {
    ChallengeEntity save(ChallengeEntity challengeEntity, UserEntity doneBy);
    Optional<ChallengeEntity> find(Integer id);

    void delete(Integer id, UserEntity doneBy);

    ChallengeEntity partialUpdate(Integer id, ChallengeEntity challengeEntity, UserEntity doneBy);

    List<ChallengeEntity> getChallenges(String email, Integer eventId);

}
