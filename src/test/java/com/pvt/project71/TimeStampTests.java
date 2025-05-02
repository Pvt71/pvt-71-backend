package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.dto.EventDto;
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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class TimeStampTests {
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
    private UserEntity fixAndSaveUser() {
        return userService.save(TestDataUtil.createValidTestUserEntity());
    }
    @Test
    public void testThatPartialUpdateEndDateIsIgnored() throws Exception {
        EventEntity testEventEntityA = TestDataUtil.createTestEventEntityA();
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);
        testEventEntityA.getAdminUsers().add(testUser);
        EventEntity savedTestEvent = eventService.save(testEventEntityA, testUser);

        EventDto eventDto = TestDataUtil.createTestEventDtoA();
        eventDto.getDates().setEndsAt(LocalDateTime.now().minusDays(1)); // Illegal date
        String eventDtoJson = objectMapper.writeValueAsString(eventDto);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/events/" + savedTestEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventDtoJson)
        ).andExpect(
                MockMvcResultMatchers.status().isOk() // Expecting 400 Bad Request
        );
    }
    @Test
    public void testCreatingChallengeThatEndsBeforeMINDURATIONGives400() throws Exception {
        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();

        testChallenge.getDates().setEndsAt(LocalDateTime.now());
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isBadRequest());
    }
    @Test
    public void testCreatingChallengeToEventThatEndsAfterTheEventShouldGive400() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        UserEntity user = fixAndSaveUser();
        userService.makeAdmin(user, testEvent);
        testEvent.getAdminUsers().add(user);
        String eventJson = objectMapper.writeValueAsString(testEvent);

        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(eventJson));

        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();

        testChallenge.setEvent(eventService.findOne(2).get());
        testChallenge.getDates().setEndsAt(testChallenge.getEvent().getDates().getEndsAt().plusHours(1));
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isBadRequest());
    }
    @Test
    public void testCreatingChallengeThatStartsAfterMaxPreSetTimeGives400() throws Exception {
        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();
        testChallenge.getDates().setStartsAt(LocalDateTime.now().plusDays(15));
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isBadRequest());
    }
    @Test
    public void testCreatingChallengeThatStartsBeforePreSetTimeAddedOnWhenEventStartsGives201() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        testEvent.getDates().setStartsAt(LocalDateTime.now().plusDays(30));

        UserEntity user = fixAndSaveUser();
        userService.makeAdmin(user, testEvent);
        testEvent.getAdminUsers().add(user);

        String eventJson = objectMapper.writeValueAsString(testEvent);

        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(eventJson));

        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();

        testChallenge.setEvent(eventService.findOne(2).get());
        testChallenge.getDates().setStartsAt(testEvent.getDates().getStartsAt().plusDays(12));

        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isCreated());
    }
    @Test
    public void testStartingEventLaterAndStartChallengeBeforeEventGives400() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        testEvent.getDates().setStartsAt(LocalDateTime.now().plusDays(30));

        UserEntity user = fixAndSaveUser();
        userService.makeAdmin(user, testEvent);
        testEvent.getAdminUsers().add(user);

        String eventJson = objectMapper.writeValueAsString(testEvent);

        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(eventJson));

        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();

        testChallenge.setEvent(eventService.findOne(2).get());
        testChallenge.getDates().setStartsAt(LocalDateTime.now());

        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isBadRequest());
    }
    @Test
    public void testStartingEventLaterAndAddChallengeWithNullStartTimeGivesEventStartTime() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        testEvent.getDates().setStartsAt(LocalDateTime.now().plusDays(30));

        UserEntity user = fixAndSaveUser();
        userService.makeAdmin(user, testEvent);
        testEvent.getAdminUsers().add(user);

        String eventJson = objectMapper.writeValueAsString(testEvent);

        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(eventJson));

        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();

        testChallenge.setEvent(eventService.findOne(2).get());

        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isCreated());
        testEvent = eventService.loadTheLazy(eventService.findOne(2).get());
        assertEquals(testEvent.getDates().getStartsAt(),
                testEvent.getChallenges().get(0).getDates().getStartsAt());
    }
    @Test
    public void testAddingChallengeToEventChangesEventsUpdatedAtTime() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        testEvent.getDates().setStartsAt(LocalDateTime.now().plusDays(30));

        UserEntity user = fixAndSaveUser();
        userService.makeAdmin(user, testEvent);
        testEvent.getAdminUsers().add(user);

        String eventJson = objectMapper.writeValueAsString(testEvent);

        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(eventJson));

        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();

        testChallenge.setEvent(eventService.findOne(2).get());
        LocalDateTime oldUpdatedAt = eventService.findOne(2).get().getDates().getUpdatedAt();
        String challengeJson = objectMapper.writeValueAsString(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isCreated());
        testEvent = eventService.loadTheLazy(eventService.findOne(2).get());
        assertNotEquals(oldUpdatedAt, testEvent.getDates().getUpdatedAt());
    }
    @Test
    public void testCreatingEventThatStartsBeforeNowGives400() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        testEvent.getDates().setStartsAt(LocalDateTime.now().minusDays(1));

        UserEntity user = fixAndSaveUser();
        userService.makeAdmin(user, testEvent);
        testEvent.getAdminUsers().add(user);

        String eventJson = objectMapper.writeValueAsString(testEvent);

        mockMvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(eventJson)).andExpect(status().isBadRequest());
    }
    @Test
    public void testCreatingChallengeThatStartsBeforeNowGives400() throws Exception {
        ChallengeEntity testChallenge = setUpChallengeEntityAWithUser();
        testChallenge.getDates().setStartsAt(LocalDateTime.now().minusDays(1));
        String challengeJson = objectMapper.writeValueAsString(testChallenge);

        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(status().isBadRequest());
    }
}
