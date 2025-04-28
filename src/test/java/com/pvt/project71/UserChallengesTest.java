package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.services.ChallengeService;
import com.pvt.project71.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UserChallengesTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UserService userService;

    private ChallengeService challengeService;

    @Autowired
    public UserChallengesTest(MockMvc mockMvc, UserService userService, ChallengeService challengeService) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.userService = userService;
        this.challengeService = challengeService;
    }

    @Test
    public void testUserChallengesListCorrectlyFilled(){
        UserEntity user = TestDataUtil.createValidTestUserEntity();
        userService.save(user);

        ChallengeEntity challengeA = TestDataUtil.createChallengeEnitityA();
        ChallengeEntity challengeB = TestDataUtil.createChallengeEnitityB();
        challengeA.setCreator(user);
        challengeB.setCreator(user);
        challengeService.save(challengeA);
        challengeService.save(challengeB);

        UserEntity reloadedUser = userService.findOne(user.getEmail()).orElseThrow();

        List<ChallengeEntity> challenges = reloadedUser.getChallenges();

        assertEquals(2, challenges.size());
        assertThat(challenges)
                .extracting(ChallengeEntity::getName)
                .containsExactlyInAnyOrder("A", "B");
    }

    @Test
    public void testUpdatedUserUpdatesChallengeCreator(){
        ChallengeEntity testChallengeA = TestDataUtil.createChallengeEnitityA();
        ChallengeEntity testChallengeB = TestDataUtil.createChallengeEnitityA();
        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        testChallengeA.setCreator(testUser);
        testChallengeB.setCreator(testUser);
        challengeService.save(testChallengeA);
        challengeService.save(testChallengeB);

        testUser.setUsername("UPDATED");
        userService.save(testUser);

        Optional<ChallengeEntity> reloadedChallengeA = challengeService.find(1);
        Optional<ChallengeEntity> reloadedChallengeB = challengeService.find(2);

        assertEquals("UPDATED", reloadedChallengeA.get().getCreator().getUsername());
        assertEquals("UPDATED", reloadedChallengeB.get().getCreator().getUsername());
    }

    @Test
    public void testUpdateChallengeCorrectlyUpdatesInUserList(){
        UserEntity user = TestDataUtil.createValidTestUserEntity();
        userService.save(user);

        ChallengeEntity challengeA = TestDataUtil.createChallengeEnitityA();
        ChallengeEntity challengeB = TestDataUtil.createChallengeEnitityB();
        challengeA.setCreator(user);
        challengeB.setCreator(user);
        challengeService.save(challengeA);
        challengeService.save(challengeB);

        UserEntity reloadedUser = userService.findOne(user.getEmail()).orElseThrow();
        List<ChallengeEntity> challenges = reloadedUser.getChallenges();

        assertEquals(2, challenges.size());
        assertThat(challenges)
                .extracting(ChallengeEntity::getName)
                .containsExactlyInAnyOrder("A", "B");

        challengeA.setName("C");
        challengeService.save(challengeA);

        reloadedUser = userService.findOne(user.getEmail()).orElseThrow();
        challenges = reloadedUser.getChallenges();

        assertEquals(2, challenges.size());
        assertThat(challenges)
                .extracting(ChallengeEntity::getName)
                .containsExactlyInAnyOrder("C", "B");
    }

    @Test
    public void testDeleteChallengeUpdatesUserList(){
        UserEntity user = TestDataUtil.createValidTestUserEntity();
        userService.save(user);

        ChallengeEntity challengeA = TestDataUtil.createChallengeEnitityA();
        ChallengeEntity challengeB = TestDataUtil.createChallengeEnitityB();
        challengeA.setCreator(user);
        challengeB.setCreator(user);
        challengeService.save(challengeA);
        challengeService.save(challengeB);

        UserEntity reloadedUser = userService.findOne(user.getEmail()).orElseThrow();
        List<ChallengeEntity> challenges = reloadedUser.getChallenges();

        assertEquals(2, challenges.size());
        assertThat(challenges)
                .extracting(ChallengeEntity::getName)
                .containsExactlyInAnyOrder("A", "B");

        challengeService.delete(1);

        reloadedUser = userService.findOne(user.getEmail()).orElseThrow();
        challenges = reloadedUser.getChallenges();

        assertEquals(1, challenges.size());
        assertThat(challenges)
                .extracting(ChallengeEntity::getName)
                .containsExactlyInAnyOrder("B");
    }

}