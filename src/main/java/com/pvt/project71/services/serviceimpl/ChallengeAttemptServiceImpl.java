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
import org.springframework.stereotype.Service;
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
    public ChallengeAttemptEntity submit(ChallengeAttemptEntity challengeAttemptEntity) throws NoSuchElementException, DuplicateKeyException {
        Optional<ChallengeEntity> challengeEntity = challengeService.find(challengeAttemptEntity.getId().getChallengeId());
        if (challengeEntity.isEmpty()) {
            throw new NoSuchElementException();
        } else if (challengeAttemptRepository.findById(challengeAttemptEntity.getId()).isPresent()) {
            throw new DuplicateKeyException("Attempt already exists");
        }
        challengeAttemptEntity.setChallenge(challengeEntity.get());
        //challengeEntity.get().getAttempts().add(challengeAttemptEntity); //Måste fixas vidare och testas massa
        if (challengeEntity.get().getProofType() == ProofType.REQUEST) {
            //it is updated then saved
            challengeAttemptEntity.setStatus(Status.PENDING);
            return save(challengeAttemptEntity);

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
