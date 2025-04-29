package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.repositories.ChallengeRepository;
import com.pvt.project71.services.ChallengeService;
import com.pvt.project71.services.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ChallengeServiceImpl implements ChallengeService {

    private static final Duration MIN_DURATION = Duration.ofMinutes(5);
    private static final Duration MAX_DURATION = Duration.ofDays(365);

    private ChallengeRepository challengeRepository;
    private EventService eventService;

    public ChallengeServiceImpl(ChallengeRepository challengeRepository, EventService eventService) {
        this.challengeRepository = challengeRepository;
        this.eventService = eventService;
    }

    /**
     *
     * @param challengeEntity Om ChallengeEntity har lämnat Event field tomt får den automatiskt standard Event
     * @return Den sparade ChallengeEntity
     */
    @Override
    @Transactional
    public ChallengeEntity save(ChallengeEntity challengeEntity) throws NoSuchElementException {
        //TODO: Lägga in logik så att man inte kan ge för mycket poäng
        //TODO: Ska fixa så att om inte en event passeras igenom får den standard event här innan det sparas
        if (challengeEntity.getEvent() == null) {
            EventEntity defaultEvent = eventService.getDefaultEvent();
            if (!checkValidDate(challengeEntity, defaultEvent)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge date is not valid");
            }
            challengeEntity.setEvent(defaultEvent);
            challengeEntity = challengeRepository.save(challengeEntity);
            defaultEvent.getChallenges().add(challengeEntity);
            eventService.save(defaultEvent);
            return challengeEntity;
        }
        Optional<EventEntity> eventEntity = eventService.findOne(challengeEntity.getEvent().getId());
        if (eventEntity.isEmpty()) {
            throw new NoSuchElementException();
        } if (!checkValidDate(challengeEntity, eventEntity.get())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge date is not valid");
        }
        challengeEntity.setEvent(eventEntity.get());
        challengeEntity = challengeRepository.save(challengeEntity);
        eventEntity.get().getChallenges().add(challengeEntity);
        eventService.save(eventEntity.get());
        return challengeEntity;
    }

    @Override
    public Optional<ChallengeEntity> find(Integer id) {
        return challengeRepository.findById(id);
    }

    @Override
    public void delete(Integer id) {
        challengeRepository.deleteById(id);
    }

    @Override
    public ChallengeEntity partialUpdate(Integer id, ChallengeEntity challengeEntity) {
        Optional<ChallengeEntity> found = challengeRepository.findById(challengeEntity.getId());
        if (found.isEmpty()) {
            throw new RuntimeException("Challenge Doesnt Exist");
        } if (challengeEntity.getEndDate() != null && !checkValidDate(challengeEntity, found.get().getEvent())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge date is not valid");
        }
        return found.map(existing -> {
            Optional.ofNullable(challengeEntity.getName()).ifPresent(existing::setName);
            Optional.ofNullable(challengeEntity.getDescription()).ifPresent(existing::setDescription);
            Optional.ofNullable(challengeEntity.getEndDate()).ifPresent(existing::setEndDate);
            Optional.ofNullable(challengeEntity.getRewardPoints()).ifPresent(existing::setRewardPoints);

            return challengeRepository.save(existing);
        }).orElseThrow(() ->new RuntimeException("Challenge Doesnt Exist"));
    }
    private boolean checkValidDate(ChallengeEntity challengeEntity, EventEntity eventEntity) {
        if (eventEntity.getId() == 1) {
            return challengeEntity.getEndDate().isAfter(LocalDateTime.now().plus(MIN_DURATION)) &&
                    challengeEntity.getEndDate().isBefore(LocalDateTime.now().plus(MAX_DURATION));
        }
        return challengeEntity.getEndDate().isAfter(LocalDateTime.now().plus(MIN_DURATION)) &&
                challengeEntity.getEndDate().compareTo(eventEntity.getEndDate()) < 1;
    }

}
