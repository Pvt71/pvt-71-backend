package com.pvt.project71.controllers;

import com.pvt.project71.domain.dto.EventDto;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.mappers.Mapper;
import com.pvt.project71.services.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class EventController {

    private EventService eventService;

    private Mapper<EventEntity, EventDto> eventMapper;

    public EventController(EventService eventService, Mapper<EventEntity, EventDto> eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @PostMapping(path = "/events")
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto event) {
        EventEntity eventEntity = eventMapper.mapFrom(event);
        EventEntity savedEvent = eventService.save(eventEntity);
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
    public ResponseEntity<EventDto> getEvent(@PathVariable("id") Long id) {
        Optional<EventEntity> foundEvent = eventService.findOne(id);
        return foundEvent.map(eventEntity -> {
            EventDto eventDto = eventMapper.mapTo(eventEntity);
            return new ResponseEntity<>(eventDto, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/events/{id}")
    public ResponseEntity<EventDto> fullUpdateEvent(
            @PathVariable("id") Long id,
            @RequestBody EventDto eventDto) {

        if (!eventService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        eventDto.setId(id);
        EventEntity eventEntity = eventMapper.mapFrom(eventDto);
        EventEntity savedEventEntity = eventService.save(eventEntity);
        return new ResponseEntity<>(
                eventMapper.mapTo(savedEventEntity),
                HttpStatus.OK
        );
    }

    @PatchMapping(path = "/events/{id}")
    public ResponseEntity<EventDto> partialUpdateEvent(
            @PathVariable("id") Long id,
            @RequestBody EventDto eventDto) {

        if (!eventService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        EventEntity eventEntity = eventMapper.mapFrom(eventDto);
        EventEntity updatedEventEntity = eventService.partialUpdate(id, eventEntity);
        return new ResponseEntity<>(eventMapper.mapTo(updatedEventEntity), HttpStatus.OK);
    }

    @DeleteMapping(path = "/events/{id}")
    public ResponseEntity deleteEvent(
            @PathVariable("id") Long id) {
        if (!eventService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        eventService.delete(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
