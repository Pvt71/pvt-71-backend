package com.pvt.project71.services.serviceimpl;



import com.pvt.project71.domain.dto.ScoreDto;
import com.pvt.project71.domain.entities.*;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.domain.entities.score.ScoreId;
import com.pvt.project71.domain.enums.ProofType;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.repositories.ChallengeAttemptRepository;
import com.pvt.project71.services.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class ChallengeAttemptServiceImpl implements ChallengeAttemptService {

    private ChallengeService challengeService;
    private ChallengeAttemptRepository challengeAttemptRepository;
    private EventService eventService;
    private ScoreService scoreService;
    private UserService userService;

    public ChallengeAttemptServiceImpl(ChallengeService challengeService, ChallengeAttemptRepository challengeAttemptRepository, EventService eventService, ScoreService scoreService, UserService userService) {
        this.challengeService = challengeService;
        this.challengeAttemptRepository = challengeAttemptRepository;
        this.eventService = eventService;
        this.scoreService = scoreService;
        this.userService = userService;
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
        if (challengeAttemptEntity.getChallenge().getCreator().getEmail().equals(challengeAttemptEntity.getId().getUserEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User can not attempt their own created challenge");
        }
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
    public ChallengeAttemptEntity accept(ChallengeAttemptEntity challengeAttemptEntity, UserEntity acceptedBy) {
        if(challengeAttemptEntity.getChallenge().getEvent().getId() == 1) {
            if (!challengeAttemptEntity.getChallenge().getCreator().equals(acceptedBy)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the challenge creator can accept this attempt");
            }
        } else if (!eventService.isAnAdmin(challengeAttemptEntity.getChallenge().getEvent(), acceptedBy)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can accept this attempt");
        } if (challengeAttemptEntity.getStatus() == Status.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attempt is already accepted");
        }
        ScoreId identifier = new ScoreId(userService.findOne(challengeAttemptEntity.getId().getUserEmail()
        ).get(), challengeAttemptEntity.getChallenge().getEvent());
        if (scoreService.findOne(identifier).isEmpty()) { //skapa en score om det inte finns
            scoreService.create(ScoreEntity.builder().scoreId(identifier)
                            .build());
        }
        scoreService.addPoints(identifier, challengeAttemptEntity.getChallenge().getRewardPoints());
        challengeAttemptEntity.setStatus(Status.ACCEPTED);
        return challengeAttemptRepository.save(challengeAttemptEntity);
    }

    @Override
    public List<ChallengeAttemptEntity> getAttemptsUserIsPermittedToAllow(UserEntity user) {
        user = userService.loadTheLazy(user);
        Set<ChallengeAttemptEntity> set = new HashSet<>(challengeAttemptRepository.findAllByChallengeCreator(user));
        for (EventEntity event : user.getEvents()) {
            set.addAll(challengeAttemptRepository.findAllByChallengeEvent(event));
        }
        return set.stream().toList();
    }

}
