package com.pvt.project71.services;

import com.pvt.project71.domain.dto.ChallengeAttemptDto;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.ChallengeAttemptEntity;
import com.pvt.project71.domain.entities.ChallengeAttemptId;
import org.springframework.dao.DuplicateKeyException;

import java.util.NoSuchElementException;
import java.util.Optional;

public interface ChallengeAttemptService {
    ChallengeAttemptEntity submit(ChallengeAttemptEntity challengeAttemptEntity) throws NoSuchElementException, DuplicateKeyException;
    Optional<ChallengeAttemptEntity> find(ChallengeAttemptId challengeAttemptId);
    ChallengeAttemptEntity save(ChallengeAttemptEntity challengeAttemptEntity);
    ChallengeAttemptEntity accept(ChallengeAttemptEntity challengeAttemptEntity);
}
