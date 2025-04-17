package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.repositories.ChallengeRepository;
import com.pvt.project71.services.ChallengeService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChallengeServiceImpl implements ChallengeService {

    private ChallengeRepository challengeRepository;

    public ChallengeServiceImpl(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    @Override
    public ChallengeEntity createChallenge(ChallengeEntity challengeEntity) {
        return challengeRepository.save(challengeEntity);
    }

    @Override
    public Optional<ChallengeEntity> find(Integer id) {
        return challengeRepository.findById(id);
    }

    @Override
    public void delete(Integer id) {
        challengeRepository.deleteById(id);
    }
}
