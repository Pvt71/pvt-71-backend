package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeAttemptRepository extends CrudRepository<ChallengeAttemptEntity, ChallengeAttemptId> {
    List<ChallengeAttemptEntity> findAllByChallengeCreator(UserEntity user);
    List<ChallengeAttemptEntity> findAllByChallengeEvent(EventEntity event);
}
