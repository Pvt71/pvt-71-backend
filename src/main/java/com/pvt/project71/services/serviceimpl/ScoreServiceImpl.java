package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.dto.EventDto;
import com.pvt.project71.domain.dto.ScoreDto;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.domain.entities.score.ScoreId;
import com.pvt.project71.repositories.ScoreRepository;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.ScoreService;
import com.pvt.project71.services.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ScoreServiceImpl implements ScoreService {
    private final ScoreRepository scoreRepository;
    private final UserService userService;
    private final EventService eventService;

    public ScoreServiceImpl(ScoreRepository scoreRepository, UserService userService, EventService eventService) {
        this.scoreRepository = scoreRepository;
        this.userService = userService;
        this.eventService = eventService;
    }


    @Override
    public Optional<ScoreEntity> create(ScoreDto scoreDto) {
        Optional<UserEntity> userOpt = userService.findOne(scoreDto.getUserDto().getEmail() );
        Optional<EventEntity> eventOpt = eventService.findOne(scoreDto.getEventId());
        //Check if user and event are valid
        if (userOpt.isEmpty() || eventOpt.isEmpty())
            return Optional.empty();
        ScoreId scoreId = ScoreId.builder().
                user(userOpt.get())
                .event(eventOpt.get()).build();
        ScoreEntity scoreEntity = ScoreEntity.builder()
                .scoreId(scoreId)
                .score(scoreDto.getScore()).build();
        return Optional.of(scoreRepository.save(scoreEntity));
    }

    @Override
    public Optional<ScoreEntity> findOne(ScoreId scoreId) {
        return   scoreRepository.findById(scoreId);
    }

    @Override
    public Optional<List<ScoreEntity>> findAllByUser(UserEntity userEntity) {
        List<ScoreEntity> scores = scoreRepository.findAllByScoreIdUserEmail(userEntity.getEmail());
        return scores.isEmpty() ? Optional.empty() : Optional.of(scores);
    }

    @Override
    public Optional<List<ScoreEntity>> findAllByEvent(int eventId) {
        List<ScoreEntity> scores = scoreRepository.findAllByScoreIdEventId(eventId);
        return scores.isEmpty() ? Optional.empty() : Optional.of(scores);
    }

    @Override
    public Optional<ScoreEntity> addPoints(ScoreId scoreId, int amount) {
        Optional<ScoreEntity>  scoreOpt =  findOne(scoreId);
        if (scoreOpt.isEmpty())
              return Optional.empty();
        ScoreEntity scoreEntity = scoreOpt.get();
        scoreEntity.setScore(scoreEntity.getScore()+amount);
        return Optional.of(scoreRepository.save(scoreEntity));
    }

    @Override
    public Optional<ScoreEntity> subtractPoints(ScoreId scoreId, int amount) {
        return addPoints(scoreId,-amount);
    }


    @Override
    public void delete(ScoreEntity scoreEntity) {
            scoreRepository.delete(scoreEntity);
    }

    @Override
    public void delete(String email, int eventId) {
        ScoreId scoreId = ScoreId.builder().user(userService.findOne(email).get())
                        .event(eventService.findOne(eventId).get())
                                .build();

        scoreRepository.deleteById(scoreId);
    }
}
