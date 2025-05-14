package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.repositories.ChallengeRepository;
import com.pvt.project71.services.ChallengeService;
import com.pvt.project71.services.EventService;
import org.modelmapper.internal.bytebuddy.implementation.bytecode.Throw;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;
import java.util.*;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ChallengeServiceImpl implements ChallengeService {

    private static final Duration MIN_DURATION = Duration.ofMinutes(5);
    private static final Duration MAX_DURATION = Duration.ofDays(365);

    //Denna är plus på events startTime om event inte börjat än
    private static final Duration MAX_PRE_CREATION_TIME = Duration.ofDays(12);

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
    public ChallengeEntity save(ChallengeEntity challengeEntity, UserEntity doneBy) {
        if (challengeEntity.getEvent() == null) {
            EventEntity defaultEvent = eventService.getDefaultEvent(doneBy.getSchool());
            if (challengeEntity.getAttempts() == null) {
                challengeEntity.setAttempts(new ArrayList<>());
            }
            if (challengeEntity.getDates().getCreatedAt() == null) {
                challengeEntity.getDates().setCreatedAt(LocalDateTime.now());
                challengeEntity.getDates().setUpdatedAt(challengeEntity.getDates().getCreatedAt());
            }

            if (challengeEntity.getDates().getStartsAt() == null) {
                challengeEntity.getDates().setStartsAt(challengeEntity.getDates().getCreatedAt());
            } if (!checkValidDate(challengeEntity, defaultEvent)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge dates is not valid");
            }

            challengeEntity.setEvent(defaultEvent);
            challengeEntity = challengeRepository.save(challengeEntity);
            defaultEvent.getChallenges().add(challengeEntity);
            eventService.save(defaultEvent, null);
            return challengeEntity;
        }
        Optional<EventEntity> eventEntity = eventService.findOne(challengeEntity.getEvent().getId());
        if (eventEntity.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event can not be found");
        }

        if (challengeEntity.getDates().getCreatedAt() == null) {
            challengeEntity.getDates().setCreatedAt(LocalDateTime.now());
            challengeEntity.getDates().setUpdatedAt(challengeEntity.getDates().getCreatedAt());
        }

        if (challengeEntity.getDates().getStartsAt() == null) {
            if (eventEntity.get().getDates().getStartsAt().equals(eventEntity.get().getDates().getCreatedAt())) {
                challengeEntity.getDates().setStartsAt(challengeEntity.getDates().getCreatedAt());
            } else {
                challengeEntity.getDates().setStartsAt(eventEntity.get().getDates().getStartsAt());
            }
        } if (!checkValidDate(challengeEntity, eventEntity.get())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge dates is not valid");
        }

        challengeEntity.setEvent(eventEntity.get());
        if (eventEntity.get().isDefault() && !challengeEntity.getCreator().equals(doneBy)) { //Om det är en challenge i default event
            //Får endast skaparn ändra på den
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator can modify this challenge");
        }
        challengeEntity = challengeRepository.save(challengeEntity);
        eventEntity.get().getChallenges().add(challengeEntity);
        eventEntity.get().getDates().setUpdatedAt(LocalDateTime.now());
        eventService.save(eventEntity.get(), challengeEntity.getCreator());
        return challengeEntity;
    }


    @Override
    @Transactional
    public Optional<ChallengeEntity> find(Integer id) {
        return challengeRepository.findById(id);
    }

    @Override
    public void delete(Integer id, UserEntity doneBy) {
        ChallengeEntity found = challengeRepository.findById(id).get();
        if (found.getEvent().isDefault()) {
            if (!found.getCreator().equals(doneBy)) { //Om det är en challenge i default event
                //Får endast skaparn ändra på den
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator can modify this challenge");
            }
        } else if (!eventService.isAnAdmin(found.getEvent(), doneBy)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not an admin");
        }
        challengeRepository.deleteById(id);
    }

    @Override
    public ChallengeEntity partialUpdate(Integer id, ChallengeEntity challengeEntity, UserEntity doneBy) {
        Optional<ChallengeEntity> found = challengeRepository.findById(challengeEntity.getId());
        if (found.isEmpty()) {
            throw new RuntimeException("Challenge Doesnt Exist");
        } if (found.get().getEvent().isDefault()) {
            if (!challengeEntity.getCreator().equals(doneBy)) { //Om det är en challenge i default event
                //Får endast skaparn ändra på den
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator can modify this challenge");
            }
        } else if (!eventService.isAnAdmin(challengeEntity.getEvent(), doneBy)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not an admin");
        }

        found.get().getDates().setUpdatedAt(LocalDateTime.now());
        return found.map(existing -> {
            Optional.ofNullable(challengeEntity.getName()).ifPresent(existing::setName);
            Optional.ofNullable(challengeEntity.getDescription()).ifPresent(existing::setDescription);
            Optional.ofNullable(challengeEntity.getPoints()).ifPresent(existing::setPoints);
            return challengeRepository.save(existing);
        }).orElseThrow(() ->new RuntimeException("Challenge Doesnt Exist"));
    }

    @Override
    public List<ChallengeEntity> getChallenges(String email, Integer eventId, String eventName) {
        if (eventId != null && email != null) {
            return challengeRepository.findByCreatorEmailAndEventId(email, eventId);
        } else if (eventId != null) {
            return challengeRepository.findChallengeEntitiesByEvent_Id(eventId);
        } else if (email != null) {
            return challengeRepository.findByCreatorEmail(email);
        } else if (eventName != null) {
            return challengeRepository.findByEventName(eventName);
        }
        List<ChallengeEntity> toReturn = new ArrayList<>();
        challengeRepository.findAll().forEach(toReturn::add);
        return toReturn;
    }

    private boolean checkValidDate(ChallengeEntity challengeEntity, EventEntity eventEntity) {
        if (!challengeEntity.getDates().getUpdatedAt().equals(challengeEntity.getDates().getCreatedAt())) {
            return true;
        } if (challengeEntity.getDates().getStartsAt().plus(MAX_DURATION).isBefore(challengeEntity.getDates().getEndsAt())) {
            return false;
        } if (challengeEntity.getDates().getStartsAt().plus(MIN_DURATION).isAfter(challengeEntity.getDates().getEndsAt())) {
            return false;
        } if (eventEntity.isDefault()) {
            return !challengeEntity.getDates().getStartsAt().isBefore(challengeEntity.getDates().getCreatedAt()) &&
                    !challengeEntity.getDates().getCreatedAt().plus(MAX_PRE_CREATION_TIME).isBefore(challengeEntity.getDates().getStartsAt());
        }
        if (eventEntity.getDates().getStartsAt().isAfter(LocalDateTime.now())) {
            if (challengeEntity.getDates().getStartsAt().isAfter(eventEntity.getDates().getStartsAt().plus(MAX_PRE_CREATION_TIME))) {
                return false;
            }
        } else if (challengeEntity.getDates().getStartsAt().isAfter(challengeEntity.getDates().getCreatedAt().plus(MAX_PRE_CREATION_TIME))) {
            return false;
        }
        return challengeEntity.getDates().getEndsAt().compareTo(eventEntity.getDates().getEndsAt()) <1 &&
                !challengeEntity.getDates().getStartsAt().isBefore(eventEntity.getDates().getStartsAt());
    }

}
