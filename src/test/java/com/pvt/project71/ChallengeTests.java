package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.repositories.ChallengeRepository;
import com.pvt.project71.services.ChallengeService;
import org.junit.jupiter.api.AfterEach;
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
public class ChallengeTests {
    @Autowired
    private ChallengeService challengeService;
    @Autowired
    private UserService userService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ChallengeRepository challengeRepository;
    @Autowired
    private EventService eventService;

    @AfterEach
    public void cleanup() {
        challengeRepository.deleteAll();
    }

    @Test
    public void testCreatingAChallengeReturns201() throws Exception{
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();

        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);
        testChallenge.setCreator(testUser);

        String challengeJson = objectMapper.writeValueAsString(testChallenge);

        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(MockMvcResultMatchers.status().isCreated());
    }
    @Test
    public void testCreatingAChallengeWithLessPointsThan1Gives404() throws Exception{
        ChallengeDto testChallenge = TestDataUtil.createChallengeDtoA();
        testChallenge.setRewardPoints(0);
        String challengeJson = objectMapper.writeValueAsString(testChallenge);

        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    @Test
    public void testCreatingAChallengeAndRetrievingIt() throws Exception{
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();

        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);
        testChallenge.setCreator(testUser);

        String challengeJson = objectMapper.writeValueAsString(testChallenge);

        mockMvc.perform(MockMvcRequestBuilders.post("/challenges").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(testChallenge.getDescription()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rewardPoints").value(testChallenge.getRewardPoints()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.endDate").value("2025-10-27T16:30"));

    }

    @Test
    public void testGetExistingChallengeGive200()throws Exception {
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();

        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);
        testChallenge.setCreator(testUser);

        ChallengeEntity saved = challengeService.save(testChallenge);

        mockMvc.perform(MockMvcRequestBuilders.get("/challenges/" + saved.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetExistingChallengeReturnsChallenge()throws Exception {
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();

        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);
        testChallenge.setCreator(testUser);

        ChallengeEntity saved = challengeService.save(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.get("/challenges/"  + saved.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(saved.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(testChallenge.getDescription()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rewardPoints").value(testChallenge.getRewardPoints()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.endDate").value("2025-10-27T16:30"));
    }

    @Test
    public void testGet404WhenChallengeDoesntExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/challenges/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testDeletingGives404WhenChallengeDoesntExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/challenges/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testDeletingGives204WhenChallengeExists() throws Exception {
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();

        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);
        testChallenge.setCreator(testUser);

        challengeService.save(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.delete("/challenges/1").contentType(MediaType.APPLICATION_JSON))
        ChallengeEntity saved = challengeService.save(testChallenge);
        mockMvc.perform(MockMvcRequestBuilders.delete("/challenges/ + " + saved.getId()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testPartialUpdateGives200OnExistingChallenge() throws  Exception {
        ChallengeEntity testChallengeA = TestDataUtil.createChallengeEnitityA();
        ChallengeEntity testChallengeB = TestDataUtil.createChallengeEnitityB();

        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        String challengeJson = objectMapper.writeValueAsString(testChallengeB);
        testChallengeA.setCreator(testUser);
        challengeService.save(testChallengeA);
        ChallengeEntity saved = challengeService.save(testChallengeA);

        mockMvc.perform(MockMvcRequestBuilders.patch("/challenges/" + saved.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testPartialUpdateReturnsUpdatedValuesOnExistingChallenge() throws  Exception {
        ChallengeEntity testChallengeA = TestDataUtil.createChallengeEnitityA();
        ChallengeEntity testChallengeB = TestDataUtil.createChallengeEnitityB();

        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        testChallengeA.setCreator(testUser);
        testChallengeB.setCreator(testUser);

        String challengeJson = objectMapper.writeValueAsString(testChallengeB);
        ChallengeEntity saved = challengeService.save(testChallengeA);

        mockMvc.perform(MockMvcRequestBuilders.patch("/challenges/" + saved.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(MockMvcResultMatchers.jsonPath("$.name").value(testChallengeB.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(testChallengeB.getDescription()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rewardPoints").value(testChallengeB.getRewardPoints()));

    }

    @Test
    public void testPartialUpdateGives404WhenChallengeDoesntExist() throws  Exception {
        ChallengeEntity testChallengeB = TestDataUtil.createChallengeEnitityB();
        String challengeJson = objectMapper.writeValueAsString(testChallengeB);

        mockMvc.perform(MockMvcRequestBuilders.patch("/challenges/1").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    @Test
    public void testFullUpdateGives200OnExistingChallenge() throws  Exception {
        ChallengeEntity testChallengeA = TestDataUtil.createChallengeEnitityA();
        ChallengeEntity testChallengeB = TestDataUtil.createChallengeEnitityB();

        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        testChallengeA.setCreator(testUser);
        testChallengeB.setCreator(testUser);

        String challengeJson = objectMapper.writeValueAsString(testChallengeB);
        ChallengeEntity saved = challengeService.save(testChallengeA);

        mockMvc.perform(MockMvcRequestBuilders.put("/challenges/" + saved.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testFullUpdateReturnsUpdatedValuesOnExistingChallenge() throws  Exception {
        ChallengeEntity testChallengeA = TestDataUtil.createChallengeEnitityA();
        ChallengeEntity testChallengeB = TestDataUtil.createChallengeEnitityB();
        String challengeJson = objectMapper.writeValueAsString(testChallengeB);
        ChallengeEntity saved = challengeService.save(testChallengeA);

        UserEntity testUser = TestDataUtil.createValidTestUserEntity();
        userService.save(testUser);

        testChallengeA.setCreator(testUser);
        testChallengeB.setCreator(testUser);

        String challengeJson = objectMapper.writeValueAsString(testChallengeB);;
        challengeService.save(testChallengeA);

        mockMvc.perform(MockMvcRequestBuilders.put("/challenges/" + saved.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(challengeJson)).andExpect(MockMvcResultMatchers.jsonPath("$.name").value(testChallengeB.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(testChallengeB.getDescription()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rewardPoints").value(testChallengeB.getRewardPoints()));

    }

    @Test
    public void testFullUpdateGives404WhenChallengeDoesntExist() throws  Exception {
        ChallengeEntity testChallengeB = TestDataUtil.createChallengeEnitityB();
        String challengeJson = objectMapper.writeValueAsString(testChallengeB);

        mockMvc.perform(MockMvcRequestBuilders.put("/challenges/1").contentType(MediaType.APPLICATION_JSON)
                .content(challengeJson)).andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    @Test
    public void testGetAllWithNoQueryInputsReturnsChallenges() throws Exception {
        ChallengeEntity testChallenge = TestDataUtil.createChallengeEnitityA();
        ChallengeEntity saved = challengeService.save(testChallenge);

        mockMvc.perform(MockMvcRequestBuilders.get("/challenges")).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(saved.getName()));
    }

}
