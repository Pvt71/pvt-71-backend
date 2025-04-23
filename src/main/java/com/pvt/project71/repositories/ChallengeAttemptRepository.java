package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.ChallengeAttemptEntity;
import com.pvt.project71.domain.entities.ChallengeAttemptId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeAttemptRepository extends CrudRepository<ChallengeAttemptEntity, ChallengeAttemptId> {
}
