package com.pvt.project71;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.dto.EventDto;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.services.EventService;
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


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)



public class EventTests {

    @Autowired
    private EventService eventService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public EventTests(MockMvc mockMvc, EventService eventService) {
        this.eventService = eventService;
        this.mockMvc = mockMvc;
    }
//    @BeforeEach
//    public void clearDatabase() {
//        eventService.findAll().forEach(event -> eventService.delete(event.getId()));
//    }

    @Test
    public void testThatCreateEventReturnsCreated() throws Exception {
        // Assert that the response status is 201 Created
        EventEntity testEvent = TestDataUtil.createTestEventEntityA(null);
        String eventJson = objectMapper.writeValueAsString(testEvent);


        mockMvc.perform(MockMvcRequestBuilders.post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(eventJson))
                .andExpect(
                        MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testThatCreateEventSuccessfullyReturnsSavedEvent () throws Exception {

        EventEntity eventEntity = TestDataUtil.createTestEventEntityA(null);
        eventEntity.setId(0);
        String eventJson = objectMapper.writeValueAsString(eventEntity);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson)
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.name").value("TestEventA")
        );
}

    @Test
    public void testThatListEventsReturnsOk() throws Exception {
        // Assert that the response status is 200 OK
        mockMvc.perform(MockMvcRequestBuilders.get("/events"))
                .andExpect(status().isOk());
    }

    @Test
    public void testThatListEventsReturnsListOfEvents() throws Exception {
        // Assert that the response contains a list of events
        EventEntity testEventA = TestDataUtil.createTestEventEntityA(null);
        eventService.save(testEventA);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/events")
                    .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[1].id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$[1].name").value("TestEventA")
        );
    }

    @Test
    public void testThatGetEventReturnsHttpStatus200IfEventExists() throws Exception {
        EventEntity testEvent = TestDataUtil.createTestEventEntityA(null);
        eventService.save(testEvent);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/events/1")
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
        EventEntity testEvent = TestDataUtil.createTestEventEntityA(null);
        eventService.save(testEvent);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/events/2")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").isNumber()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value("TestEventA")
        );
    }

    @Test
    public void testThatFullUpdateEventReturnsHttpStatus404WhenNoEventExists() throws Exception {
        EventDto testEvent = TestDataUtil.createTestEventDtoA(null);
        String eventJson = objectMapper.writeValueAsString(testEvent);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/events/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson)
        ).andExpect(
                MockMvcResultMatchers.status().isNotFound()
        );
    }

    @Test
    public void testThatFullUpdateEventReturnsHttpStatus200WhenEventExists() throws Exception {
        EventEntity testEventEntity = TestDataUtil.createTestEventEntityA(null);
        EventEntity savedEvent = eventService.save(testEventEntity);

        EventDto testEventDto = TestDataUtil.createTestEventDtoA(savedEvent);
        String eventJson = objectMapper.writeValueAsString(testEventDto);
        mockMvc.perform(
                MockMvcRequestBuilders.put("/events/" + savedEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson)
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        );
    }

    @Test
    public void testThatFullUpdateUpdatesExistingEvent() throws Exception {
        EventEntity testEventEntity = TestDataUtil.createTestEventEntityA(null);
        EventEntity savedEvent = eventService.save(testEventEntity);

        EventDto testEventDto = TestDataUtil.createTestEventDtoA(null);
        testEventDto.setId(savedEvent.getId());

        String eventUpdateJson = objectMapper.writeValueAsString(testEventDto);


        mockMvc.perform(
                MockMvcRequestBuilders.put("/events/" + savedEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventUpdateJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(savedEvent.getId())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value(testEventDto.getName())
        );
    }

    @Test
    public void testThatPartialUpdateEventReturnsHttpStatus200IfUserExists() throws Exception {
        //EventEntity testProjectA = TestDataUtil.createTestEventEntityA(TestDataUtil.createTestUserEntityA());
        EventEntity testProjectA = TestDataUtil.createTestEventEntityA(null);
        EventEntity savedTestEvent = eventService.save(testProjectA);

        //EventDto eventDto = TestDataUtil.createTestEventDtoA(TestDataUtil.createTestUserEntityA());
        EventDto eventDto = TestDataUtil.createTestEventDtoA(null);
        String eventDtoJson = objectMapper.writeValueAsString(eventDto);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/events/" + savedTestEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventDtoJson)
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testThatPartialUpdateEventUpdatesExistingUser() throws Exception {
        //EventEntity testProjectA = TestDataUtil.createTestEventEntityA(TestDataUtil.createTestUserEntityA());
        EventEntity testEventEntityA = TestDataUtil.createTestEventEntityA(null);
        EventEntity savedTestEvent = eventService.save(testEventEntityA);

        //EventDto eventDto = TestDataUtil.createTestEventDtoA(TestDataUtil.createTestUserEntityA());
        EventDto eventDto = TestDataUtil.createTestEventDtoA(null);
        eventDto.setName("UPDATED");
        String eventDtoJson = objectMapper.writeValueAsString(eventDto);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/events/" + savedTestEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventDtoJson)
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.id").value(savedTestEvent.getId())
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.name").value("UPDATED")
        );
    }

    @Test
    public void testThatDeleteTaskReturnsHttpStatus204IfUserExist () throws Exception {
        //EventEntity testProjectA = TestDataUtil.createTestEventEntityA(TestDataUtil.createTestUserEntityA());
        EventEntity testEventEntityA = TestDataUtil.createTestEventEntityA(null);
        EventEntity savedTestEvent = eventService.save(testEventEntityA);

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/events/" + savedTestEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testThatDeleteProjectReturnsHttpStatus204IfUserDontExist () throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/events/999")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

}
