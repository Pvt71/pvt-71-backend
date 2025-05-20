package com.pvt.project71.services.serviceimpl;



import com.pvt.project71.domain.dto.ChallengeAttemptDto;
import com.pvt.project71.domain.entities.*;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.domain.entities.score.ScoreId;
import com.pvt.project71.domain.enums.ProofType;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.repositories.ChallengeAttemptRepository;
import com.pvt.project71.services.*;
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
        if (challengeAttemptEntity.getChallenge().getCreator().getEmail().trim().equalsIgnoreCase((challengeAttemptEntity.getId().getUserEmail().trim()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User can not attempt their own created challenge");
        }

        if (challengeEntity.get().getProofType() == ProofType.REQUEST) {
            addChallengeAttemptToChallenge(challengeAttemptEntity, challengeEntity.get());
            return challengeAttemptEntity;

        } if (challengeEntity.get().getProofType() == ProofType.CONTENT) {
            String dummyContent = "HiddenGoob";//Vet inte hur en challenges content ska sparas, kanske i entityn?
            String content = dummyContent; //Todo: Fixa rätt när det finns
            if (challengeAttemptEntity.getContent().equals(content)) {
                addChallengeAttemptToChallenge(challengeAttemptEntity, challengeEntity.get());
                accept(challengeAttemptEntity, challengeEntity.get().getCreator());
                challengeAttemptEntity.setStatus(Status.ACCEPTED);
                return challengeAttemptEntity;
            } throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content sent was not equal to the expected content");
        } else { //annars är det paircontent
            List<ChallengeAttemptEntity> matchingAttempts = challengeAttemptRepository.findAllByChallengeAndContent(
                    challengeEntity.get(), challengeAttemptEntity.getContent());
            if (matchingAttempts.isEmpty()) {
                return addChallengeAttemptToChallenge(challengeAttemptEntity, challengeEntity.get());
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Content is already in use");
            }
        }
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
        if(challengeAttemptEntity.getChallenge().getEvent().isDefault()) {
            if (!challengeAttemptEntity.getChallenge().getCreator().equals(acceptedBy)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the challenge creator can accept this attempt");
            }
        } else if (!userService.isAnAdmin(acceptedBy, challengeAttemptEntity.getChallenge().getEvent() )) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can accept this attempt");
        } if (challengeAttemptEntity.getStatus() == Status.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attempt is already accepted");
        } if (challengeAttemptEntity.getId().getUserEmail().equals(acceptedBy.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin can not accept their own attempt");
        }
        ScoreId identifier = new ScoreId(userService.findOne(challengeAttemptEntity.getId().getUserEmail()
        ).get(), challengeAttemptEntity.getChallenge().getEvent());
        if (scoreService.findOne(identifier).isEmpty()) { //skapa en score om det inte finns
            scoreService.create(ScoreEntity.builder().scoreId(identifier)
                            .build());
        }
        scoreService.addPoints(identifier, challengeAttemptEntity.getChallenge().getPoints());
        challengeAttemptEntity.setStatus(Status.ACCEPTED);
        return challengeAttemptRepository.save(challengeAttemptEntity);
    }

    @Override
    public List<ChallengeAttemptEntity> getAttemptsUserCanAllow(UserEntity user) {
        user = userService.loadTheLazy(user);
        Set<ChallengeAttemptEntity> set = new HashSet<>(challengeAttemptRepository.findAllByChallengeCreatorAndChallengeProofTypeAndStatus(user, ProofType.REQUEST,
                Status.PENDING));
        for (EventEntity event : user.getEvents()) {
            set.addAll(challengeAttemptRepository.findAllByChallengeEventAndChallengeProofTypeAndStatus(event
                    , ProofType.REQUEST, Status.PENDING));
        }
        return set.stream().toList();
    }

    @Override
    public List<ChallengeAttemptEntity> getAttemptsSubmittedByUser(String userEmail) {
        return challengeAttemptRepository.findAllByIdUserEmail(userEmail);
    }

    @Override
    public ChallengeAttemptEntity sync(ChallengeAttemptEntity challengeAttemptEntity) {
        Optional<ChallengeEntity> challenge = challengeService.find(challengeAttemptEntity.getId().getChallengeId());
        if (challenge.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge doesnt exist");
        }

        List<ChallengeAttemptEntity> matchingAttempts = challengeAttemptRepository.findAllByChallengeAndContent(
                challenge.get(), challengeAttemptEntity.getContent());
        if (matchingAttempts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The Content to pair with didnt exist");
        } else if (matchingAttempts.size() > 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Content is already paired");
        }
        addChallengeAttemptToChallenge(challengeAttemptEntity, challenge.get());
        return acceptPairContent(challengeAttemptEntity, matchingAttempts.get(0));
    }

    private ChallengeAttemptEntity addChallengeAttemptToChallenge(ChallengeAttemptEntity challengeAttemptEntity, ChallengeEntity challengeEntity ) {
        challengeEntity.getAttempts().add(challengeAttemptEntity);
        challengeAttemptEntity.setStatus(Status.PENDING);
        challengeService.save(challengeEntity, challengeEntity.getCreator());
        return challengeAttemptEntity;
    }

    /**
     * Lägger till poäng för båda användarna
     * @param scanner Den som skickar in den existerande content koden
     * @param owner Den som skapade content koden
     * @return uppdaterad scanner där det är accepted
     */
    private ChallengeAttemptEntity acceptPairContent(ChallengeAttemptEntity scanner, ChallengeAttemptEntity owner) {
        accept(owner, owner.getChallenge().getCreator());
        return accept(scanner, owner.getChallenge().getCreator());
    }

}
