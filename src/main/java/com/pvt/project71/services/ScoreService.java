package com.pvt.project71.services;


import com.pvt.project71.domain.dto.EventDto;
import com.pvt.project71.domain.dto.ScoreDto;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.domain.entities.score.ScoreId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ScoreService {

      ScoreEntity create(ScoreEntity scoreEntity);
      Optional<ScoreEntity> findOne(ScoreId scoreId);

      Optional<List<ScoreEntity>> findAllByUser(UserEntity userEntity);
      Optional<List<ScoreEntity>> findAllByEvent(int event);
      Optional<ScoreEntity> addPoints(ScoreId scoreId, int amount);
      Optional<ScoreEntity> subtractPoints(ScoreId scoreId, int amount);
      void  delete(ScoreEntity scoreEntity);
      void  delete(String email, int eventId);
}
