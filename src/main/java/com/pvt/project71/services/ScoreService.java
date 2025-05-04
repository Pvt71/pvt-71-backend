package com.pvt.project71.services;


import com.pvt.project71.domain.dto.EventDto;
import com.pvt.project71.domain.dto.ScoreDto;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.domain.entities.score.ScoreId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ScoreService {


      Optional<ScoreEntity> create(ScoreDto scoreDto);
      Optional<ScoreEntity> findOne(ScoreId scoreId);
      Optional<ScoreEntity> findOne(String email, int eventId);
      Optional<List<ScoreEntity>> findAllByUser(String email);
      Optional<List<ScoreEntity>> findAllByEvent(int  eventId);
      void  delete(ScoreEntity scoreEntity);
      void  delete(String email, int eventId);
}
