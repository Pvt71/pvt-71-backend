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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UserChallengesIntegrationTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UserService userService;

    private ChallengeService challengeService;

    @Autowired
    public UserChallengesIntegrationTest(MockMvc mockMvc, UserService userService, ChallengeService challengeService) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
        this.userService = userService;
        this.challengeService = challengeService;
    }
    @Test
    public void testRetrieveOneChallengeFromUser() throws Exception {
        UserEntity userEntity = TestDataUtil.createValidTestUserEntity();
        userService.save(userEntity);

        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        challengeEntity.setCreator(userEntity);
        challengeService.save(challengeEntity);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/challenges")
                        .param("user", "Test@test.com")
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.size()").value(1)
        );
    }

    @Test
    public void testRetrieveMultipleChallengesFromUser() throws Exception {
        UserEntity userEntity = TestDataUtil.createValidTestUserEntity();
        userService.save(userEntity);

        ChallengeEntity challengeEntityA = TestDataUtil.createChallengeEnitityA();
        challengeEntityA.setCreator(userEntity);
        challengeService.save(challengeEntityA);

        ChallengeEntity challengeEntityB = TestDataUtil.createChallengeEnitityB();
        challengeEntityB.setCreator(userEntity);
        challengeService.save(challengeEntityB);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/challenges")
                        .param("user", "Test@test.com")
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.size()").value(2)
        );
    }

    @Test
    public void testUpdateUserUpdatesCreatorInChallenges() throws Exception {
        UserEntity userEntity = TestDataUtil.createValidTestUserEntity();
        userService.save(userEntity);

        ChallengeEntity challengeEntityA = TestDataUtil.createChallengeEnitityA();
        challengeEntityA.setCreator(userEntity);
        challengeService.save(challengeEntityA);

        ChallengeEntity challengeEntityB = TestDataUtil.createChallengeEnitityB();
        challengeEntityB.setCreator(userEntity);
        challengeService.save(challengeEntityB);

        userEntity.setUsername("UPDATED");
        userService.save(userEntity);

        mockMvc.perform(MockMvcRequestBuilders.get("/challenges")
                        .param("user", userEntity.getEmail())
        ).andExpect(MockMvcResultMatchers.status().isOk()
        ).andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2)
        ).andExpect(MockMvcResultMatchers.jsonPath("$[0].creator.username").value("UPDATED")
        ).andExpect(MockMvcResultMatchers.jsonPath("$[1].creator.username").value("UPDATED")
        );
    }

    @Test
    public void testUpdateChallengeUpdatesUsersChallenges() throws Exception {
        UserEntity userEntity = TestDataUtil.createValidTestUserEntity();
        userService.save(userEntity);

        ChallengeEntity challengeEntityA = TestDataUtil.createChallengeEnitityA();
        challengeEntityA.setCreator(userEntity);
        challengeService.save(challengeEntityA);

        mockMvc.perform(MockMvcRequestBuilders.get("/challenges")
                .param("user", userEntity.getEmail())
        ).andExpect(MockMvcResultMatchers.status().isOk()
        ).andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1)
        ).andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("A")
        );

        challengeEntityA.setName("UPDATED");
        challengeService.save(challengeEntityA);

        mockMvc.perform(MockMvcRequestBuilders.get("/challenges")
                .param("user", userEntity.getEmail())
        ).andExpect(MockMvcResultMatchers.status().isOk()
        ).andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1)
        ).andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("UPDATED")
        );
    }

    @Test
    public void testDeleteChallengeCorrectlyUpdatesUserChallenges() throws Exception {
        UserEntity userEntity = TestDataUtil.createValidTestUserEntity();
        userService.save(userEntity);

        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        challengeEntity.setCreator(userEntity);
        challengeEntity = challengeService.save(challengeEntity);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/challenges")
                        .param("user", "Test@test.com")
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.size()").value(1)
        );

        challengeService.delete(challengeEntity.getId());

        mockMvc.perform(
                MockMvcRequestBuilders.get("/challenges")
                        .param("user", "Test@test.com")
        ).andExpect(
                MockMvcResultMatchers.status().isOk()
        ).andExpect(
                MockMvcResultMatchers.jsonPath("$.size()").value(0)
        );
    }

    @Test
    public void testNoChallengesRetrievedForNonExistingUser() throws Exception {
            UserEntity userEntity = TestDataUtil.createValidTestUserEntity();
            userService.save(userEntity);

            ChallengeEntity challengeEntityA = TestDataUtil.createChallengeEnitityA();
            challengeEntityA.setCreator(userEntity);
            challengeService.save(challengeEntityA);

            ChallengeEntity challengeEntityB = TestDataUtil.createChallengeEnitityB();
            challengeEntityB.setCreator(userEntity);
            challengeService.save(challengeEntityB);

            mockMvc.perform(
                    MockMvcRequestBuilders.get("/challenges")
                            .param("user", "doesnot@exist.com")
            ).andExpect(
                    MockMvcResultMatchers.status().isOk()
            ).andExpect(
                    MockMvcResultMatchers.jsonPath("$.size()").value(0)
            );
    }

}