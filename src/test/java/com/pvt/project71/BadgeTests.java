package com.pvt.project71;

import com.pvt.project71.domain.TimeStamps;
import com.pvt.project71.domain.entities.EventEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.services.EventService;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.ScoreService;
import com.pvt.project71.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class BadgeTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private EventService eventService;
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private JwtService jwtService;

    private Jwt getUserToken(UserEntity userEntity){
        return jwtService.mockOauth2(userEntity, 1, ChronoUnit.MINUTES);
    }

    private Jwt getExpiredUserToken(UserEntity userEntity){
        return jwtService.mockOauth2(userEntity, 1,ChronoUnit.NANOS);
    }

    @Test
    public void testOneUserCanReceiveOneBadge() throws Exception {
        UserEntity user = TestDataUtil.createValidTestUserEntity();
        userService.save(user);

        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        testEvent.setAdminUsers(List.of(user));
        byte[] fakeImage = TestDataUtil.createTestImageBytes();
        testEvent.setBadgePicture(fakeImage);
        eventService.save(testEvent, user);

        ScoreEntity testScore = TestDataUtil.createValidScoreEntity(user, testEvent);
        scoreService.create(testScore);
        assertEquals(testScore.getScoreId().getUser(), user);

        eventService.giveBadges(testEvent);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/" + user.getEmail())
                        .with(jwt().jwt(getUserToken(user)))
        ).andExpect(jsonPath("$.badges", hasSize(1))
        ).andExpect(jsonPath("$.badges[0].description").value("You were rank 1 in event: " + testEvent.getName()));
    }

    @Test
    public void testOneUserCanReceiveMultipleBadges() throws Exception {
        UserEntity user = TestDataUtil.createValidTestUserEntity();
        userService.save(user);

        //Set up events
        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        testEvent.setAdminUsers(List.of(user));
        byte[] fakeImage = TestDataUtil.createTestImageBytes();
        testEvent.setBadgePicture(fakeImage);
        eventService.save(testEvent, user);

        EventEntity testEvent2 = TestDataUtil.createTestEventEntityA();
        testEvent2.setAdminUsers(List.of(user));
        testEvent2.setBadgePicture(fakeImage);
        eventService.save(testEvent2, user);

        //Set up scores (joining the event)
        ScoreEntity testScore = TestDataUtil.createValidScoreEntity(user, testEvent);
        scoreService.create(testScore);
        assertEquals(testScore.getScoreId().getUser(), user);

        ScoreEntity testScore2 = TestDataUtil.createValidScoreEntity(user, testEvent2);
        scoreService.create(testScore2);

        //Give badges
        eventService.giveBadges(testEvent);
        eventService.giveBadges(testEvent2);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/" + user.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(getUserToken(user)))
        ).andExpect(jsonPath("$.badges", hasSize(2))
        ).andExpect(jsonPath("$.badges[0].description").value("You were rank 1 in event: " + testEvent.getName())
        ).andExpect(jsonPath("$.badges[1].description").value("You were rank 1 in event: " + testEvent.getName()));
    }

    @Test
    public void testMultipleUsersCanReceiveOneBadge() throws Exception {
        UserEntity userA = TestDataUtil.createValidTestUserEntity();
        UserEntity userB = TestDataUtil.createValidTestUserEntityB();
        userService.save(userA);
        userService.save(userB);

        EventEntity testEvent = TestDataUtil.createTestEventEntityA();
        testEvent.setAdminUsers(List.of(userA, userB));
        byte[] fakeImage = TestDataUtil.createTestImageBytes();
        testEvent.setBadgePicture(fakeImage);
        eventService.save(testEvent, userA);

        ScoreEntity testScore = TestDataUtil.createValidScoreEntity(userA, testEvent);
        testScore.setScore(10);
        ScoreEntity testScore2 = TestDataUtil.createValidScoreEntity(userB, testEvent);
        testScore2.setScore(20);
        scoreService.create(testScore);
        scoreService.create(testScore2);

        eventService.giveBadges(testEvent);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/" + userA.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(getUserToken(userA)))
        ).andExpect(jsonPath("$.badges", hasSize(1))
        ).andExpect(jsonPath("$.badges[0].description").value("You were rank 2 in event: " + testEvent.getName()));

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/" + userB.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(getUserToken(userB)))
        ).andExpect(jsonPath("$.badges", hasSize(1))
        ).andExpect(jsonPath("$.badges[0].description").value("You were rank 1 in event: " + testEvent.getName()));
    }

    @Test
    public void testMultipleUsersCanReceiveMultipleBadges() throws Exception {
        UserEntity userA = TestDataUtil.createValidTestUserEntity();
        UserEntity userB = TestDataUtil.createValidTestUserEntityB();
        userService.save(userA);
        userService.save(userB);

        EventEntity testEventA = TestDataUtil.createTestEventEntityA();
        testEventA.setAdminUsers(List.of(userA, userB));
        byte[] fakeImage = TestDataUtil.createTestImageBytes();
        testEventA.setBadgePicture(fakeImage);
        eventService.save(testEventA, userA);

        EventEntity testEventB = TestDataUtil.createTestEventEntityA();
        testEventB.setAdminUsers(List.of(userA, userB));
        testEventB.setBadgePicture(fakeImage);
        eventService.save(testEventB, userA);

        ScoreEntity testScore = TestDataUtil.createValidScoreEntity(userA, testEventA);
        testScore.setScore(10);
        ScoreEntity testScore2 = TestDataUtil.createValidScoreEntity(userB, testEventA);
        testScore2.setScore(20);
        ScoreEntity testScore3 = TestDataUtil.createValidScoreEntity(userA, testEventB);
        testScore3.setScore(100);
        ScoreEntity testScore4 = TestDataUtil.createValidScoreEntity(userB, testEventB);
        testScore4.setScore(20);
        scoreService.create(testScore);
        scoreService.create(testScore2);
        scoreService.create(testScore3);
        scoreService.create(testScore4);

        eventService.giveBadges(testEventA);
        eventService.giveBadges(testEventB);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/" + userA.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(getUserToken(userA)))
        ).andExpect(jsonPath("$.badges", hasSize(2))
        ).andExpect(jsonPath("$.badges[0].description").value("You were rank 2 in event: " + testEventA.getName())
        ).andExpect(jsonPath("$.badges[1].description").value("You were rank 1 in event: " + testEventB.getName()));

        mockMvc.perform(
                MockMvcRequestBuilders.get("/users/" + userB.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(getUserToken(userB)))
        ).andExpect(jsonPath("$.badges", hasSize(2))
        ).andExpect(jsonPath("$.badges[0].description").value("You were rank 1 in event: " + testEventA.getName())
        ).andExpect(jsonPath("$.badges[1].description").value("You were rank 2 in event: " + testEventB.getName()));
    }

}
