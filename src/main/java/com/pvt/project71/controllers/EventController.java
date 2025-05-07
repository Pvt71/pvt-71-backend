package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.EventDto;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.mappers.Mapper;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class EventController {

    private EventService eventService;
    private UserService userService;

    private Mapper<EventEntity, EventDto> eventMapper;
    private JwtService jwtService;

    public EventController(EventService eventService, UserService userService, Mapper<EventEntity, EventDto> eventMapper, JwtService jwtService) {
        this.eventService = eventService;
        this.userService = userService;
        this.eventMapper = eventMapper;
        this.jwtService = jwtService;
    }

    @PostMapping(path = "/events")
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto event, @AuthenticationPrincipal Jwt userToken) {
        if (event.getDates() == null) {
            return new ResponseEntity<EventDto>(HttpStatus.BAD_REQUEST);
        }if (event.getDates().getEndsAt() == null) {
            return new ResponseEntity<EventDto>(HttpStatus.BAD_REQUEST);
        } if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        event.getDates().setCreatedAt(null);
        EventEntity eventEntity = eventMapper.mapFrom(event);

        Optional<UserEntity> creator = userService.findOne(userToken.getSubject());
        //Optional<UserEntity> creator = userService.findOne("Test@test.com"); //TODO: Ska bort sen
        if (creator.isEmpty()) {
            return new ResponseEntity<EventDto>(HttpStatus.UNAUTHORIZED);
        }
        userService.makeAdmin(creator.get(), eventEntity);
        eventEntity.setAdminUsers(new ArrayList<>());
        eventEntity.getAdminUsers().add(creator.get());
        EventEntity savedEvent = eventService.save(eventEntity, creator.get());
        return new ResponseEntity<>(eventMapper.mapTo(savedEvent), HttpStatus.CREATED);
    }

    @GetMapping(path = "/events")
    @ResponseBody
    public List<EventDto> listAuthors() {
        List<EventEntity> events = eventService.findAll();
        return events.stream()
                .map(eventMapper::mapTo)
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/events/{id}")
    public ResponseEntity<EventDto> getEvent(@PathVariable("id") Integer id) {
        Optional<EventEntity> foundEvent = eventService.findOne(id);
        return foundEvent.map(eventEntity -> {
            EventDto eventDto = eventMapper.mapTo(eventEntity);
            return new ResponseEntity<>(eventDto, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/events/{id}")
    public ResponseEntity<EventDto> fullUpdateEvent(
            @PathVariable("id") Integer id,
            @RequestBody EventDto eventDto,
            @AuthenticationPrincipal Jwt userToken) {

        if (!eventService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } if (id == 1 || userToken == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); //Ingen får ändra på default event
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        //Optional<UserEntity> user = userService.findOne("Test@test.com"); //TODO: Ska bort sen
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        EventEntity found = eventService.findOne(id).get();
        eventDto.setId(id);
        eventDto.setDates(found.getDates());
        eventDto.getDates().setUpdatedAt(LocalDateTime.now());
        EventEntity eventEntity = eventMapper.mapFrom(eventDto);
        eventEntity.setAdminUsers(found.getAdminUsers());
        EventEntity savedEventEntity = eventService.save(eventEntity, user.get());
        return new ResponseEntity<>(
                eventMapper.mapTo(savedEventEntity),
                HttpStatus.OK
        );
    }

    @PatchMapping(path = "/events/{id}")
    public ResponseEntity<EventDto> partialUpdateEvent(
            @PathVariable("id") Integer id,
            @RequestBody EventDto eventDto,
            @AuthenticationPrincipal Jwt userToken) {

        if (!eventService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } if (id == 1) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }if (userToken == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        EventEntity eventEntity = eventMapper.mapFrom(eventDto);
        EventEntity updatedEventEntity = eventService.partialUpdate(id, eventEntity, user.get());
        return new ResponseEntity<>(eventMapper.mapTo(updatedEventEntity), HttpStatus.OK);
    }

    @DeleteMapping(path = "/events/{id}")
    public ResponseEntity deleteEvent(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal Jwt userToken) {
        if (!eventService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } if (id == 1) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        eventService.delete(id, user.get());
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    //Admin för events
    @PatchMapping(path = "/events/{id}/admins/add/{email}")
    public ResponseEntity<EventDto> addAdminToEvent(@PathVariable("id") Integer id,
                                                    @PathVariable("email") String email,
                                                    @AuthenticationPrincipal Jwt userToken) {
        if (!eventService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } if (id == 1) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); //Ingen får ändra på default event
        }
        Optional<UserEntity> toAdd = userService.findOne(email);
        EventEntity eventEntity = eventService.findOne(id).get();
        if (toAdd.isEmpty()) {
            return new ResponseEntity<EventDto>(HttpStatus.NOT_FOUND);
        } if (eventEntity.getAdminUsers().contains(toAdd)) {
            return new ResponseEntity<EventDto>(HttpStatus.CONFLICT);
        } if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        //Optional<UserEntity> user = userService.findOne("Test@test.com"); //TODO: Ska bort sen
        if (user.isEmpty()) {
            return new ResponseEntity<EventDto>(HttpStatus.UNAUTHORIZED);
        }
        eventEntity = eventService.addAdmin(eventEntity, toAdd.get(), user.get());
        return new ResponseEntity<>(eventMapper.mapTo(eventEntity), HttpStatus.OK);
    }

    @PatchMapping(path = "/events/{id}/admins/leave")
    public ResponseEntity<EventDto> leaveEventAsAdmin(@PathVariable("id") Integer id,
                                                      @AuthenticationPrincipal Jwt userToken) {
        if (!eventService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } if (id == 1) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); //Ingen får ändra på default event
        } if (!jwtService.isTokenValid(userToken)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        EventEntity eventEntity = eventService.findOne(id).get();

        Optional<UserEntity> user = userService.findOne(userToken.getSubject());
        //Optional<UserEntity> user = userService.findOne("Test@test.com"); //TODO: Ska bort sen
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        eventEntity = eventService.removeAdmin(eventEntity, user.get());
        return new ResponseEntity<>(eventMapper.mapTo(eventEntity), HttpStatus.OK);
    }

}
