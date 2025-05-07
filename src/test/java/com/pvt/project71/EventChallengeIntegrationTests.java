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
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
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
    @Autowired
    private JwtService jwtService;

    /**
     * Skapar UserEntity och sparar den i repository
     * @return ChallengeEntity med userEntity
     */
    @AfterEach
    void setUp() {
        // Clean up the database after each test
        for (EventEntity e : eventRepository.findAll()) {
            if (e.getId() != 1) {
                eventRepository.delete(e);
            }
        }
        challengeRepository.deleteAll();
    }

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
    private UserEntity fixAndSaveUser() {
        return userService.save(TestDataUtil.createValidTestUserEntity());
    }
    private Jwt getUserToken() {
        return jwtService.mockOauth2(TestDataUtil.createValidTestUserEntity(),1, ChronoUnit.MINUTES);
    }
    @Test
    //@Transactional
    public void testCreatingChallengeWithoutASpecifiedEventShouldGetDefaultEvent() throws Exception {
        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();
        fixAndSaveUser();
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson).with(jwt().jwt(getUserToken()))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.event.id").value(1));
    }
    @Test
    public void testCreatingChallengeWithoutASpecifiedEventAndRetrieveChallengeViaTheEvent() throws Exception {
        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        fixAndSaveUser();
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson).with(jwt().jwt(getUserToken())));
        assertFalse(eventService.loadTheLazy(eventService.getDefaultEvent()).getChallenges().isEmpty());

    }

    @Test
    public void testAddingChallengeToCustomEventWorks() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        String eventJson = objectMapper.writeValueAsString(testEvent);
        UserEntity user = fixAndSaveUser();
        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(eventJson)
                .with(jwt().jwt(getUserToken())));

        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();
        testChallenge.setEvent(eventService.findOne(2).get());
        String challengeJson = objectMapper.writeValueAsString(testChallenge);

        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson).with(jwt().jwt(getUserToken()))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.event.id").value(2L));
    }
    @Test
    public void testAddingChallengeToCustomEventAndRetrievingItViaEvent() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        UserEntity user = fixAndSaveUser();
        String eventJson = objectMapper.writeValueAsString(testEvent);

        testEvent = eventService.save(testEvent, user);

        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();

        testChallenge.setEvent(testEvent);

        String challengeJson = objectMapper.writeValueAsString(testChallenge);

        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson));
        assertFalse(eventService.loadTheLazy(testEvent).getChallenges().isEmpty());
    }
    @Test
    public void testAddingChallengeToCustomEventButWithANonAdminUserGives403() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        fixAndSaveUser();
        String eventJson = objectMapper.writeValueAsString(testEvent);

        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(eventJson).with(jwt().jwt(getUserToken())));

        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        testUser.setEmail("Gooby@gmail.com");
        userService.save(testUser);
        testChallenge.setCreator(testUser);
        testChallenge.setEvent(eventService.findOne(2).get());
        String challengeJson = objectMapper.writeValueAsString(testChallenge);

        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson).with(jwt().jwt(jwtService.mockOauth2(testUser, 1, ChronoUnit.MINUTES)))).andExpect(status().isForbidden());

    }
    @Test
    public void testModifyingCreatedChallengeInDefaultEventAsUserThatDidntCreateItGives403() throws Exception {
        fixAndSaveUser();
        UserEntity userb = TestDataUtil.createValidTestUserEntityB();
        userb = userService.save(userb);
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();
        testChallenge.setCreator(userb);
        testChallenge = challengeService.save(testChallenge, userb);
        mockMvc.perform(MockMvcRequestBuilders.patch("/challenges/" +testChallenge.getId())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(testChallenge))
                .with(jwt().jwt(getUserToken()))).andExpect(status().isForbidden());
    }
    @Test
    public void testAddingChallengeToNonExistingEventShouldGive404() throws Exception {
        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();

        testChallenge.setEvent(EventEntity.builder().id(4).build());

        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson).with(jwt().jwt(getUserToken()))).andExpect(status().isNotFound());
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
        testEvent.getAdminUsers().add(testChallengeA.getCreator());
        eventService.save(testEvent, testChallengeA.getCreator());


        challengeService.save(testChallengeA, testChallengeA.getCreator());
        challengeService.save(testChallengeB, testChallengeB.getCreator());
        challengeService.save(testChallengeC, testChallengeC.getCreator());


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
        testEvent.getAdminUsers().add(testChallengeA.getCreator());
        eventService.save(testEvent, testChallengeA.getCreator());
        challengeService.save(testChallengeA, testChallengeA.getCreator());

        mockMvc.perform(MockMvcRequestBuilders.delete("/events/{id}", testEvent.getId()).with(jwt().jwt(getUserToken())))
                .andExpect(status().isNoContent());

        assertTrue(challengeRepository.findById(testChallengeA.getId()).isEmpty());
    }


}
