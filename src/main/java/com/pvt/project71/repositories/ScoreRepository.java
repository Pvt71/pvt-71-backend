package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.domain.entities.score.ScoreId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends CrudRepository<ScoreEntity, ScoreId> {

      List<ScoreEntity> findAllByScoreIdUserEmail(String email);

}
