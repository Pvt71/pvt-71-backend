package com.pvt.project71;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.dto.EventDto;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.repositories.EventRepository;
import com.pvt.project71.repositories.UserRepository;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")


public class EventTests {

    @Autowired
    private EventService eventService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;

    //    @BeforeEach
//    public void clearDatabase() {
//        eventService.findAll().forEach(event -> eventService.delete(event.getId()));
//    }
    @AfterEach
    public void clearDatabase() {
        eventService.findAll().forEach(event -> {
            if (event.isDefault())  // Assuming ID 1 is the default event
                eventRepository.deleteById(event.getId());
            });
        userRepository.deleteAll();
    }
    private Jwt getUserToken() {
        return jwtService.mockOauth2(TestDataUtil.createValidTestUserEntity(),1, ChronoUnit.MINUTES);
    }
    private UserEntity fixAndSaveUser() {
        return userService.save(TestDataUtil.createValidTestUserEntity());
    }
    @Test
    public void testThatCreateEventReturnsCreated() throws Exception {
        // Assert that the response status is 201 Created
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        String eventJson = objectMapper.writeValueAsString(testEvent);
        fixAndSaveUser();

        mockMvc.perform(MockMvcRequestBuilders.post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson)
                        .with(jwt().jwt(getUserToken()))
        )
                .andExpect(
                        MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testThatCreateEventSuccessfullyReturnsSavedEvent() throws Exception {
        EventEntity eventEntity = TestDataUtil.createTestEventEntityA();
        String eventJson = objectMapper.writeValueAsString(eventEntity);
        fixAndSaveUser();
        mockMvc.perform(
                MockMvcRequestBuilders.post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson)
                        .with(jwt().jwt(getUserToken()))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value("TestEventA")
        );
    }

    @Test
    public void testThatListEventsReturnsOk() throws Exception {
        // Assert that the response status is 200 OK
        fixAndSaveUser();
        mockMvc.perform(MockMvcRequestBuilders.get("/events").with(jwt().jwt(getUserToken())))
                .andExpect(status().isOk());
    }

    @Test
    public void testThatListEventsReturnsListOfEvents() throws Exception {
        // Assert that the response contains a list of events
        EventEntity testEventA = TestDataUtil.createTestEventEntityA();
        UserEntity user = fixAndSaveUser();
        user.getEvents().add(testEventA);
        testEventA.getAdminUsers().add(user);
        testEventA = eventService.save(testEventA, user);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/events")
                        .contentType(MediaType.APPLICATION_JSON).with(jwt().jwt(getUserToken()))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[0].name").value("TestEventA")
        );
    }

    @Test
    public void testThatGetEventReturnsHttpStatus200IfEventExists() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        UserEntity user = fixAndSaveUser();
        testEvent.getAdminUsers().add(user);
        EventEntity saved = eventService.save(testEvent, user);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/events/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatGetEventReturnsHttpStatus404IfNoEventExists() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/events/99")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testThatGetEventReturnsEventIfEventExists() throws Exception {
        UserEntity user = fixAndSaveUser();
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        testEvent.getAdminUsers().add(user);
        user.getEvents().add(testEvent);
        EventEntity saved = eventService.save(testEvent, user);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/events/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(getUserToken()))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value("TestEventA")
        );
    }

    @Test
    public void testThatFullUpdateEventReturnsHttpStatus404WhenNoEventExists() throws Exception {
        EventDto testEvent = TestDataUtil.createTestEventDtoA();
        String eventJson = objectMapper.writeValueAsString(testEvent);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/events/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson)
                        .with(jwt().jwt(getUserToken()))
        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }

    @Test
    public void testThatFullUpdateEventReturnsHttpStatus200WhenEventExists() throws Exception {
        EventEntity testEventEntity = TestDataUtil.createTestEventEntityA();
        UserEntity user = fixAndSaveUser();
        testEventEntity.getAdminUsers().add(user);
        user.getEvents().add(testEventEntity);
        EventEntity savedEvent = eventService.save(testEventEntity, user);

        EventDto testEventDto = TestDataUtil.createTestEventDtoA();
        String eventJson = objectMapper.writeValueAsString(testEventDto);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/events/" + savedEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson)
                        .with(jwt().jwt(getUserToken()))
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatFullUpdateUpdatesExistingEvent() throws Exception {
        EventEntity testEventEntity = TestDataUtil.createTestEventEntityA();
        UserEntity user = fixAndSaveUser();
        testEventEntity.getAdminUsers().add(user);
        user.getEvents().add(testEventEntity);
        EventEntity savedEvent = eventService.save(testEventEntity, user);

        EventDto testEventDto = TestDataUtil.createTestEventDtoA();
        testEventDto.setId(savedEvent.getId());

        String eventUpdateJson = objectMapper.writeValueAsString(testEventDto);


        mockMvc.perform(
                MockMvcRequestBuilders.put("/events/" + savedEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventUpdateJson)
                        .with(jwt().jwt(getUserToken()))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(savedEvent.getId())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value(testEventDto.getName())
        );
    }

    @Test
    public void testThatPartialUpdateEventReturnsHttpStatus200IfUserExists() throws Exception {
        //EventEntity testProjectA = TestDataUtil.createTestEventEntityA(TestDataUtil.createTestUserEntityA());
        EventEntity testProjectA = TestDataUtil.createTestEventEntityA();
        UserEntity user = fixAndSaveUser();
        testProjectA.getAdminUsers().add(user);
        user.getEvents().add(testProjectA);
        EventEntity savedTestEvent = eventService.save(testProjectA, user);

        //EventDto eventDto = TestDataUtil.createTestEventDtoA(TestDataUtil.createTestUserEntityA());
        EventDto eventDto = TestDataUtil.createTestEventDtoA();
        String eventDtoJson = objectMapper.writeValueAsString(eventDto);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/events/" + savedTestEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventDtoJson)
                        .with(jwt().jwt(getUserToken()))
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatPartialUpdateEventUpdatesExistingUser() throws Exception {
        //EventEntity testProjectA = TestDataUtil.createTestEventEntityA(TestDataUtil.createTestUserEntityA());
        EventEntity testEventEntityA = TestDataUtil.createTestEventEntityA();
        UserEntity user = fixAndSaveUser();
        testEventEntityA.getAdminUsers().add(user);
        user.getEvents().add(testEventEntityA);
        EventEntity savedTestEvent = eventService.save(testEventEntityA, user);


        //EventDto eventDto = TestDataUtil.createTestEventDtoA(TestDataUtil.createTestUserEntityA());
        EventDto eventDto = TestDataUtil.createTestEventDtoA();
        eventDto.setName("UPDATED");
        String eventDtoJson = objectMapper.writeValueAsString(eventDto);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/events/" + savedTestEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventDtoJson)
                        .with(jwt().jwt(getUserToken()))
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(savedTestEvent.getId())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value("UPDATED")
        );
    }

    @Test
    public void testThatDeleteTaskReturnsHttpStatus204IfUserExist() throws Exception {
        //EventEntity testProjectA = TestDataUtil.createTestEventEntityA(TestDataUtil.createTestUserEntityA());
        EventEntity testEventEntityA = TestDataUtil.createTestEventEntityA();
        UserEntity user = fixAndSaveUser();
        testEventEntityA.getAdminUsers().add(user);
        user = userService.loadTheLazy(user);
        user.getEvents().add(testEventEntityA);
        EventEntity savedTestEvent = eventService.save(testEventEntityA, user);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/events/" + savedTestEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(getUserToken()))
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatDeleteProjectReturnsHttpStatus204IfUserDontExist() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/events/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(getUserToken()))
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    //ADmin tests
    @Test
    public void testAddingANewAdminWorkAndItExistsViaUserServicesToo() throws Exception {
        EventEntity testEventEntity = TestDataUtil.createTestEventEntityA();
        UserEntity user = fixAndSaveUser();
        testEventEntity.getAdminUsers().add(user);
        user.getEvents().add(testEventEntity);
        EventEntity savedEvent = eventService.save(testEventEntity, user);

        UserEntity userB = TestDataUtil.createValidTestUserEntity();
        userB.setEmail("test2@test.com");
        userB = userService.save(userB);
        mockMvc.perform(MockMvcRequestBuilders.patch("/events/" + savedEvent.getId() + "/admins/add/" + userB.getEmail())
                        .with(jwt().jwt(getUserToken())))
                .andExpect(status().isOk());
        savedEvent = eventService.findOne(savedEvent.getId()).get();

        assertEquals(2, savedEvent.getAdminUsers().size());
        assertEquals(savedEvent, userService.loadTheLazy(userB).getEvents().get(0));

    }
    @Test
    public void testRemovingAdminWorks() throws Exception {
        EventEntity testEventEntity = TestDataUtil.createTestEventEntityA();
        UserEntity user = fixAndSaveUser();
        testEventEntity.getAdminUsers().add(user);
        user.getEvents().add(testEventEntity);
        EventEntity savedEvent = eventService.save(testEventEntity, user);

        UserEntity userB = TestDataUtil.createValidTestUserEntity();
        userB.setEmail("test2@test.com");
        userB = userService.save(userB);
        userB = userService.makeAdmin(userB, savedEvent, user);
        savedEvent = eventService.findOne(savedEvent.getId()).get();
        mockMvc.perform(MockMvcRequestBuilders.patch("/events/" + savedEvent.getId() + "/admins/leave")
                        .with(jwt().jwt(getUserToken())))
                .andExpect(status().isOk());

        savedEvent = eventService.findOne(savedEvent.getId()).get();
        assertEquals(1, savedEvent.getAdminUsers().size());
        assertEquals(0, userService.loadTheLazy(user).getEvents().size());

    }
    @Test
    public void testRemovingEventShouldRemoveItFromTheAdminUsersListAndUserShouldPersist() throws Exception {
        EventEntity testEventEntity = TestDataUtil.createTestEventEntityA();
        UserEntity user = fixAndSaveUser();
        testEventEntity.getAdminUsers().add(user);
        user.getEvents().add(testEventEntity);
        EventEntity savedEvent = eventService.save(testEventEntity, user);
        mockMvc.perform(MockMvcRequestBuilders.delete("/events/" + savedEvent.getId())
                .with(jwt().jwt(getUserToken())));
        Optional<UserEntity> found = userService.findOne(user.getEmail());
        assertTrue(found.isPresent());
        assertTrue(userService.loadTheLazy(found.get()).getEvents().isEmpty());
    }
    @Test
    public void testModifyingDefaultEventShouldGive403() throws Exception {
        EventEntity defaultEvent = eventService.getDefaultEvent(TestDataUtil.SCHOOL_NAME);
        fixAndSaveUser();
        mockMvc.perform(MockMvcRequestBuilders.delete("/events/" + defaultEvent.getId())
                .with(jwt().jwt(getUserToken()))).andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.patch("/events/" + defaultEvent.getId() + "/admins/add/random@mail.se" )
                        .with(jwt().jwt(getUserToken())))
                .andExpect(status().isForbidden());
        mockMvc.perform(
                MockMvcRequestBuilders.patch("/events/" + defaultEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(defaultEvent))
                        .with(jwt().jwt(getUserToken()))
        ).andExpect(status().isForbidden());
    }
    @Test
    public void testCreatingTwoDifferentDefaultEventsWorks() throws Exception {
        EventEntity defaultSU = eventService.getDefaultEvent("Stockholm Universitet");
        EventEntity detaulftKTH = eventService.getDefaultEvent("KTH");
        assertTrue(detaulftKTH.isDefault());
        assertTrue(defaultSU.isDefault());
    }
}


