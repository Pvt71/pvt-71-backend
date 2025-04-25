package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.repositories.ChallengeRepository;
import com.pvt.project71.services.ChallengeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class ChallengeServiceImpl implements ChallengeService {

    private ChallengeRepository challengeRepository;

    public ChallengeServiceImpl(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    /**
     *
     * @param challengeEntity Om ChallengeEntity har lämnat Event field tomt får den automatiskt standard Event
     * @return Den sparade ChallengeEntity
     */
    @Override
    public ChallengeEntity save(ChallengeEntity challengeEntity) {
        //TODO: Lägga in logik så att man inte kan ge för mycket poäng
        //TODO: Ska fixa så att om inte en event passeras igenom får den standard event här innan det sparas
        if (challengeEntity.getAttempts() == null) {
            challengeEntity.setAttempts(new ArrayList<>());
        }
        return challengeRepository.save(challengeEntity);
    }

    @Override
    @Transactional
    public Optional<ChallengeEntity> find(Integer id) {
        return challengeRepository.findById(id);
    }

    @Override
    public void delete(Integer id) {
        challengeRepository.deleteById(id);
    }

    @Override
    public ChallengeEntity partialUpdate(Integer id, ChallengeEntity challengeEntity) {
        return challengeRepository.findById(id).map(existing -> {
            Optional.ofNullable(challengeEntity.getName()).ifPresent(existing::setName);
            Optional.ofNullable(challengeEntity.getDescription()).ifPresent(existing::setDescription);
            Optional.ofNullable(challengeEntity.getEndDate()).ifPresent(existing::setEndDate);
            Optional.ofNullable(challengeEntity.getRewardPoints()).ifPresent(existing::setRewardPoints);

            return challengeRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Challenge doesnt exist"));
    }

}
