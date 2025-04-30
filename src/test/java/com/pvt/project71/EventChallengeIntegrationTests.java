package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.mappers.mapperimpl.EventMapper;
import com.pvt.project71.repositories.ChallengeRepository;
import com.pvt.project71.repositories.EventRepository;
import com.pvt.project71.services.ChallengeService;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EventChallengeIntegrationTests {
    @Autowired
    private ChallengeService challengeService;
    @Autowired
    private EventService eventService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private ChallengeRepository challengeRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserService userService;

    /**
     * Skapar UserEntity och sparar den i repository
     * @return ChallengeEntity med userEntity
     */
    private ChallengeEntity setUpChallengeEntityAWithUser() {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();
        testChallenge.setCreator(testUser);
        return testChallenge;
    }
    private ChallengeEntity setUpChallengeEntityBWithUser() {
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityB();
        testChallenge.setCreator(testUser);
        return testChallenge;
    }

    @Test
    //@Transactional
    public void testCreatingChallengeWithoutASpecifiedEventShouldGetDefaultEvent() throws Exception {
        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.event").value(eventMapper.mapTo(eventService.getDefaultEvent())));
    }
    @Test
    public void testCreatingChallengeWithoutASpecifiedEventAndRetrieveChallengeViaTheEvent() throws Exception {
        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson));
        assertFalse(eventService.loadTheLazy(eventService.getDefaultEvent()).getChallenges().isEmpty());

    }

    @Test
    public void testAddingChallengeToCustomEventWorks() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        String eventJson = objectMapper.writeValueAsString(testEvent);

        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(eventJson));

        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();
        testChallenge.setEvent(eventService.findOne(2).get());
        String challengeJson = objectMapper.writeValueAsString(testChallenge);

        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.event.id").value(2L));
    }
    @Test
    public void testAddingChallengeToCustomEventAndRetrievingItViaEvent() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        String eventJson = objectMapper.writeValueAsString(testEvent);

        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(eventJson));

        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();

        testChallenge.setEvent(eventService.findOne(2).get());
        String challengeJson = objectMapper.writeValueAsString(testChallenge);

        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson));
        assertFalse(eventService.loadTheLazy(eventService.findOne(2).get()).getChallenges().isEmpty());
    }
    @Test
    public void testAddingChallengeToNonExistingEventShouldGive404() throws Exception {
        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();

        testChallenge.setEvent(EventEntity.builder().id(4).build());
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isNotFound());
    }
    @Test
    public void testCreatingChallengeThatEndsBeforeMINDURATIONGives400() throws Exception {
        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();

        testChallenge.setEndDate(LocalDateTime.now());
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isBadRequest());
    }
    @Test
    public void testCreatingChallengeToEventThatEndsAfterTheEventShouldGive400() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        String eventJson = objectMapper.writeValueAsString(testEvent);

        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(eventJson));

        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();

        testChallenge.setEvent(eventService.findOne(2).get());
        testChallenge.setEndDate(testChallenge.getEvent().getEndDate().plusHours(1));
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isBadRequest());
    }

    @Test
    public void testGetChallengesByEventId() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        ChallengeEntity testChallengeA = setUpChallengeEntityAWithUser();

        ChallengeEntity testChallengeB = setUpChallengeEntityBWithUser();

        ChallengeEntity testChallengeC = setUpChallengeEntityBWithUser();
        testChallengeC.setName("UPDATED");


        testChallengeA.setEvent(testEvent);
        testChallengeB.setEvent(testEvent);
        eventService.save(testEvent);


        challengeService.save(testChallengeA);
        challengeService.save(testChallengeB);
        challengeService.save(testChallengeC);


        mockMvc.perform(MockMvcRequestBuilders.get("/challenges").param("eventId", testEvent.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(testChallengeA.getName()))
                .andExpect(jsonPath("$[1].name").value(testChallengeB.getName()))
                .andExpect(jsonPath("$[2]").doesNotExist()
            );
    }

    @Test
    public void testThatDeleteEventDeletesChallenges() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        ChallengeEntity testChallengeA = setUpChallengeEntityAWithUser();

        testChallengeA.setEvent(testEvent);
        eventService.save(testEvent);
        challengeService.save(testChallengeA);

        mockMvc.perform(MockMvcRequestBuilders.delete("/events/{id}", testEvent.getId()))
                .andExpect(status().isNoContent());

        assertTrue(challengeRepository.findById(testChallengeA.getId()).isEmpty());
    }


}
