package com.pvt.project71;

import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.repositories.NotificationRepository;
import com.pvt.project71.repositories.UserRepository;
import com.pvt.project71.services.ChallengeService;
import com.pvt.project71.services.security.JwtService;
import com.pvt.project71.services.NotificationService;
import com.pvt.project71.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class NotificationTests {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ChallengeService challengeService;

    @AfterEach
    public void cleanup() {
        userRepository.deleteAll();
    }

    private Jwt getUserToken(UserEntity userEntity){
        return jwtService.generateTokenFromUserEntity(userEntity, 1, ChronoUnit.MINUTES);
    }

    private UserEntity fixAndSaveUser() {
        return userService.save(TestDataUtil.createValidTestUserEntity());
    }

    @Test
    public void testBaseNotificationStuffWorksAsIntended() throws Exception {
        UserEntity user = fixAndSaveUser();
        assertFalse(user.isNewNotifications());

        notificationService.add(user, "Notification1");
        user = userService.findOne(user.getEmail()).get();
        assertTrue(user.isNewNotifications());
        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/fetch").with(jwt().jwt(getUserToken(user))))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].content").value("Notification1"));

    }
    @Test
    public void testSubmittingAChallengeAttemptGivesCreatorNotification() throws Exception {
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        UserEntity userB = userService.save(TestDataUtil.createValidTestUserEntityB());
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit")
                .with(jwt().jwt(getUserToken(userB))));
        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/anyNew").with(jwt().jwt(getUserToken(user))))
                .andExpect(status().isOk());
    }
    @Test
    public void testRejectingAttemptGivesUserNotification() throws Exception {
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        UserEntity userB = userService.save(TestDataUtil.createValidTestUserEntityB());
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit")
                .with(jwt().jwt(getUserToken(userB))));
        mockMvc.perform(patch("/challenges/"+challengeEntity.getId() +"/reject/" + userB.getEmail())
                .with(jwt().jwt(getUserToken(user))));
        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/anyNew").with(jwt().jwt(getUserToken(userB))))
                .andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/fetch").with(jwt().jwt(getUserToken(userB))))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0]").exists());
    }
}
