package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.dto.ChallengeDto;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.mappers.mapperimpl.EventMapper;
import com.pvt.project71.services.ChallengeService;
import com.pvt.project71.services.EventService;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

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

    @Test
    //@Transactional
    public void testCreatingChallengeWithoutASpecifiedEventShouldGetDefaultEvent() throws Exception {
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.event").value(eventMapper.mapTo(eventService.getDefaultEvent())));
    }
    @Test
    @Transactional
    public void testCreatingChallengeWithoutASpecifiedEventAndRetrieveChallengeViaTheEvent() throws Exception {
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson));
        assertFalse(eventService.getDefaultEvent().getChallenges().isEmpty());
    }
    @Test
    public void testAddingChallengeToCustomEventWorks() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA(null);
        String eventJson = objectMapper.writeValueAsString(testEvent);


        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(eventJson));
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();
        testChallenge.setEvent(eventService.findOne(2L).get());
        String challengeJson = objectMapper.writeValueAsString(testChallenge);

        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.event.id").value(2L));
    }
    @Test
    @Transactional
    public void testAddingChallengeToCustomEventAndRetrievingItViaEvent() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA(null);
        String eventJson = objectMapper.writeValueAsString(testEvent);


        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(eventJson));
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();
        testChallenge.setEvent(eventService.findOne(2L).get());
        String challengeJson = objectMapper.writeValueAsString(testChallenge);

        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson));
        assertFalse(eventService.findOne(2L).get().getChallenges().isEmpty());
    }
    @Test
    public void testAddingChallengeToNonExistingEventShouldGive404() throws Exception {
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();
        testChallenge.setEvent(EventEntity.builder().id(4).build());
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isNotFound());
    }
}
