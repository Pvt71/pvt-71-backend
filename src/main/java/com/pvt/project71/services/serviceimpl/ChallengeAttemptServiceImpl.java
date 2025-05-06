package com.pvt.project71.services.serviceimpl;



import com.pvt.project71.domain.entities.ChallengeAttemptEntity;
import com.pvt.project71.domain.entities.ChallengeAttemptId;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.enums.ProofType;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.repositories.ChallengeAttemptRepository;
import com.pvt.project71.services.ChallengeAttemptService;
import com.pvt.project71.services.ChallengeService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ChallengeAttemptServiceImpl implements ChallengeAttemptService {

    private ChallengeService challengeService;
    private ChallengeAttemptRepository challengeAttemptRepository;

    public ChallengeAttemptServiceImpl(ChallengeService challengeService, ChallengeAttemptRepository challengeAttemptRepository) {
        this.challengeService = challengeService;
        this.challengeAttemptRepository = challengeAttemptRepository;
    }

    @Override
    public ChallengeAttemptEntity submit(ChallengeAttemptEntity challengeAttemptEntity) {
        Optional<ChallengeEntity> challengeEntity = challengeService.find(challengeAttemptEntity.getId().getChallengeId());
        if (challengeEntity.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge doesnt exist");
        } else if (challengeAttemptRepository.findById(challengeAttemptEntity.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Submission already exists");
        }
        challengeAttemptEntity.setChallenge(challengeEntity.get());
        challengeEntity.get().getAttempts().add(challengeAttemptEntity);
        if (challengeEntity.get().getProofType() == ProofType.REQUEST) {
            challengeAttemptEntity.setStatus(Status.PENDING);
            challengeService.save(challengeEntity.get(), challengeEntity.get().getCreator());
            return challengeAttemptEntity;

        }
        //Annars ska logik för hur det anses vara accepterat köras
        //TODO: implementera logiken

            //För nu ska man inte ens nå det här
        return null;
    }

    @Override
    public Optional<ChallengeAttemptEntity> find(ChallengeAttemptId challengeAttemptId) {
        return challengeAttemptRepository.findById(challengeAttemptId);
    }

    @Override
    public ChallengeAttemptEntity save(ChallengeAttemptEntity challengeAttemptEntity) {
        return challengeAttemptRepository.save(challengeAttemptEntity);
    }

    @Override
    public ChallengeAttemptEntity accept(ChallengeAttemptEntity challengeAttemptEntity) {
        Optional<ChallengeAttemptEntity> existing = challengeAttemptRepository.findById(challengeAttemptEntity.getId());
        //TODO: Score ska ges till användaren
        existing.get().setStatus(Status.ACCEPTED);
        return challengeAttemptRepository.save(existing.get());
    }

}
