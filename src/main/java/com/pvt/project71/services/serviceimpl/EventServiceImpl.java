package com.pvt.project71.services.serviceimpl;

import com.pvt.project71.domain.TimeStamps;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.repositories.EventRepository;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class EventServiceImpl implements EventService {

    private EventRepository eventRepository;
    private UserService userService;
    private static final Duration MAX_PRE_CREATION_TIME = Duration.ofDays(30);
    private static final Duration MIN_DURATION_HOURS = Duration.ofHours(24);
    private static final Duration MAX_DURATION_DAYS = Duration.ofDays(365);

    public EventServiceImpl (EventRepository eventRepository, @Lazy UserService userService) {
        this.eventRepository = eventRepository;
        this.userService = userService;
    }

    @Override
    public EventEntity save(EventEntity eventEntity, UserEntity doneBy) {
        if (eventEntity.getChallenges() == null) {
            eventEntity.setChallenges(new ArrayList<>());
        } if (eventEntity.getScores() == null) {
            eventEntity.setScores(new ArrayList<>());
        }if (eventEntity.getDates().getCreatedAt() == null) {
            eventEntity.getDates().setCreatedAt(LocalDateTime.now());
            eventEntity.getDates().setUpdatedAt(eventEntity.getDates().getCreatedAt());
        }if (eventEntity.getDates().getStartsAt() == null) {
            eventEntity.getDates().setStartsAt(eventEntity.getDates().getCreatedAt());
        } if (eventEntity.getParticipants() == null) {
            eventEntity.setParticipants(0);
        }

        if (!userService.isAnAdmin(doneBy, eventEntity)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not an admin");
        }

        if (eventEntity.getId() == null) {
            if (checkValidDate(eventEntity)) {
                return eventRepository.save(eventEntity);
            }
        } else if (eventEntity.isDefault() || checkValidDate(eventEntity)){ //Default event ignoreras att göra date check
            return eventRepository.save(eventEntity);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event date is not valid!");
    }

    @Override
    public List<EventEntity> findAll(){
        return StreamSupport.stream(eventRepository
                        .findAll(Sort.by(Sort.Direction.DESC, "dates.updatedAt"))
                        .spliterator()
                        , false)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<EventEntity> findOne(Integer id) {
        return eventRepository.findById(id);
    }

    @Override
    public boolean isExists(Integer id) {
        return eventRepository.existsById(id);
    }

    @Override
    public EventEntity partialUpdate(Integer id, EventEntity eventEntity, UserEntity doneBy) throws ResponseStatusException {
        eventEntity.setId(id);
        eventEntity.getDates().setUpdatedAt(LocalDateTime.now());
        eventEntity.getDates().setUpdatedAt(LocalDateTime.now());
        return eventRepository.findById(id).map(existingEvent -> {
            Optional.ofNullable(eventEntity.getName()).ifPresent(existingEvent::setName);
            Optional.ofNullable(eventEntity.getDescription()).ifPresent(existingEvent::setDescription);
            Optional.ofNullable(eventEntity.getBannerImage()).ifPresent(existingEvent::setBannerImage);
            return eventRepository.save(existingEvent);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event does not exist!"));
    }

    @Override
    public void delete(Integer id, UserEntity doneBy) {
        if (!userService.isAnAdmin(doneBy, eventRepository.findById(id).get())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not an Admin");
        }
        eventRepository.deleteById(id);
    }

    @Override
    @Transactional
    public EventEntity getDefaultEvent(String school) {
        Optional<EventEntity> defaultEvent = eventRepository.findByName(school);
        if (defaultEvent.isEmpty()) {
            return eventRepository.save(EventEntity.builder().name(school).challenges(new ArrayList<>())
                    .dates(TimeStamps.builder().startsAt(LocalDateTime.now()).createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now()).build()).isDefault(true).participants(0).scores(new ArrayList<>()).school(school).build());
        }
        return defaultEvent.get();
    }

    /**
     * Laddar in Lazyloaded relationer
     */
    @Override
    @Transactional
    public EventEntity loadTheLazy(EventEntity toLoad) {
        EventEntity eventEntity = eventRepository.findById(toLoad.getId()).get();
        eventEntity.getChallenges().isEmpty();
        eventEntity.getScores().isEmpty();
        return eventEntity;
    }

    private boolean checkValidDate(EventEntity eventEntity) {
        if (!eventEntity.getDates().getCreatedAt().equals(eventEntity.getDates().getUpdatedAt())) {
            return true;
        } if (eventEntity.getDates().getStartsAt().isBefore(eventEntity.getDates().getCreatedAt())) {
            return false;
        } if (eventEntity.getDates().getCreatedAt().plus(MAX_PRE_CREATION_TIME).isBefore(eventEntity.getDates().getStartsAt())) {
            return false;
        } return !eventEntity.getDates().getStartsAt().plus(MAX_DURATION_DAYS).isBefore(eventEntity.getDates().getEndsAt())
                && !eventEntity.getDates().getStartsAt().plus(MIN_DURATION_HOURS).isAfter(eventEntity.getDates().getEndsAt());
    }



    @Override
    public List<EventEntity> findAllBySchool(String school, UserEntity whoWantsThem) {
        List<EventEntity> events = eventRepository.findBySchool(school);
        for (EventEntity e : events) {
            if (e.getAdminUsers().contains(whoWantsThem)) {
                e.setAreYouAdmin(true);
            }
        }
        return events;
    }



}
