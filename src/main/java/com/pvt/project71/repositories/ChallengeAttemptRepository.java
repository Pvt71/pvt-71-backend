package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.*;
import com.pvt.project71.domain.enums.ProofType;
import com.pvt.project71.domain.enums.Status;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeAttemptRepository extends CrudRepository<ChallengeAttemptEntity, ChallengeAttemptId> {
    List<ChallengeAttemptEntity> findAllByChallengeCreatorAndChallengeProofTypeAndStatus(UserEntity user, ProofType proofType, Status status);
    List<ChallengeAttemptEntity> findAllByChallengeEventAndChallengeProofTypeAndStatus(EventEntity event, ProofType proofType, Status status);
    List<ChallengeAttemptEntity> findAllByIdUserEmail(String userEmail);
    List<ChallengeAttemptEntity> findAllByChallengeAndContent(ChallengeEntity challenge, String content);
}
