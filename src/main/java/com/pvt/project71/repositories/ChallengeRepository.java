package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.ChallengeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends CrudRepository<ChallengeEntity, Integer> {

    List<ChallengeEntity> findByCreatorEmail(String email);

}
