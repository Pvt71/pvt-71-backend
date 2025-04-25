package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.dto.ChallengeAttemptDto;
import com.pvt.project71.domain.entities.ChallengeAttemptEntity;
import com.pvt.project71.domain.entities.ChallengeAttemptId;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.mappers.mapperimpl.ChallengeAttemptMapper;
import com.pvt.project71.repositories.ChallengeAttemptRepository;
import com.pvt.project71.repositories.ChallengeRepository;
import com.pvt.project71.services.ChallengeAttemptService;
import com.pvt.project71.services.ChallengeService;
import com.pvt.project71.services.UserService;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class ChallengeAttemptTests {
    private final String CONTENT = "goobfulTestButThisCanBeLeftBlank";

    @Autowired
    private ChallengeService challengeService;
    @Autowired
    private UserService userService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ChallengeAttemptRepository challengeAttemptRepository;
    @Autowired
    private ChallengeAttemptMapper challengeAttemptMapper;
    @Autowired
    private ChallengeRepository challengeRepository;
    @Autowired
    private ChallengeAttemptService challengeAttemptService;

    @AfterEach
    public void cleanup() {challengeAttemptRepository.deleteAll(); challengeRepository.deleteAll();}
    //Mapper Tests
    @Test
    public void testCreatingEntityAndMapToDTOIsDoneCorrectly() {
        ChallengeAttemptEntity challengeAttemptEntity = ChallengeAttemptEntity.builder()
                .id(new ChallengeAttemptId(1, TestDataUtil.createValidTestUserEntity().getEmail()))
                .status(Status.ACCEPTED).challenge(TestDataUtil.createChallengeEnitityA()).build();
        ChallengeAttemptDto challengeAttemptDto = challengeAttemptMapper.mapTo(challengeAttemptEntity);
        assertAll(
                () -> assertEquals(TestDataUtil.createValidTestUserEntity().getEmail(), challengeAttemptDto.getUserEmail()),
                () -> assertEquals(TestDataUtil.createChallengeDtoA(), challengeAttemptDto.getChallenge()),
                () -> assertEquals(challengeAttemptEntity.getStatus(), challengeAttemptDto.getStatus())
        );
    }
    @Test
    public void testCreatingDtoAndMapToEntityIsDoneCorrectly() {
        ChallengeAttemptDto challengeAttemptDto = ChallengeAttemptDto.builder().status(Status.ACCEPTED)
                .challenge(TestDataUtil.createChallengeDtoA())
                .userEmail(TestDataUtil.createValidTestUserDtoA().getEmail()).build();
        ChallengeAttemptEntity challengeAttemptEntity = challengeAttemptMapper.mapFrom(challengeAttemptDto);
        assertAll(
                () -> assertEquals(challengeAttemptDto.getStatus(), challengeAttemptEntity.getStatus()),
                () -> assertEquals(challengeAttemptDto.getUserEmail(), challengeAttemptEntity.getId().getUserEmail()),
                () -> assertEquals(challengeAttemptDto.getChallenge().getId(), challengeAttemptEntity.getId().getChallengeId())
        );
    }
    @Test
    public void testSubmittingAnAttemptToExistingChallengeWorksCorrectly() throws Exception{

        ChallengeEntity challengeEntity = challengeService.save(TestDataUtil.createChallengeEnitityA());
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT))
                .andExpect(status().isCreated())
                .andExpectAll(
                        jsonPath("$.status").value("PENDING"),
                        jsonPath("$.userEmail").value("Test@Test.com"),//Var mail som var bestämt för nu i controller
                        jsonPath("$.challenge.id").value(challengeEntity.getId()),//Måste fixas när entityDTo har listan med attempts med sig
                        jsonPath("$.content").value(CONTENT));
    }
    @Test
    public void testSubmittingAnAttemptToANonExistingChallengeShouldGive404() throws Exception {
        mockMvc.perform(post("/challenges/" +1 +"/submit/" + CONTENT))
                .andExpect(status().isNotFound());
    }
    @Test
    public void testSubmittingAnAttemptCorrectlyAndCheckItIsSavedCorrectlyInRepository() throws Exception {
        ChallengeEntity challengeEntity = challengeService.save(TestDataUtil.createChallengeEnitityA());
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT));
        assertTrue(challengeAttemptService.find(new ChallengeAttemptId(challengeEntity.getId(), "Test@Test.com")).isPresent());
    }
    @Test
    public void testSubmittingAnAttemptToExistingChallengeWithAttemptFromUserAlreadyGives409() throws Exception {
        ChallengeEntity challengeEntity = challengeService.save(TestDataUtil.createChallengeEnitityA());
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT));
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT + "littleDifferent"))
                .andExpect(status().isConflict());
    }
    @Test
    @Transactional
    public void testSubmittingAnAttemptToExistingChallengeAndRetrieveItViaTheChallengeShouldGiveAttempt() throws Exception {
        ChallengeEntity challengeEntity = challengeService.save(TestDataUtil.createChallengeEnitityA());
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT));
        challengeEntity = challengeService.find(challengeEntity.getId()).get();
            assertFalse(challengeEntity.getAttempts().isEmpty());
    }
    @Test
    public void testAcceptingASubmittedAttempt() throws Exception{
        ChallengeEntity challengeEntity = challengeService.save(TestDataUtil.createChallengeEnitityA());
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/Test@Test.com"))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.status").value("ACCEPTED"),
                        jsonPath("$.userEmail").value("Test@Test.com"),//Var mail som var bestämt för nu i controller
                        jsonPath("$.challenge.id").value(challengeEntity.getId()),
                        jsonPath("$.content").value(CONTENT));
    }
    @Test
    public void testAcceptingAnUnsubmittedAttemptShouldGive404() throws Exception {
        ChallengeEntity challengeEntity = challengeService.save(TestDataUtil.createChallengeEnitityA());
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/Test@Test.com"))
                .andExpect(status().isNotFound());
    }
    @Test
    @Transactional
    public void testAcceptingSubmittedAttemptAndCheckItIsUpdatedInChallengesAttemptListCorrectly() throws Exception {
        ChallengeEntity challengeEntity = challengeService.save(TestDataUtil.createChallengeEnitityA());
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/Test@Test.com"));
        challengeEntity = challengeService.find(challengeEntity.getId()).get();
        assertEquals(Status.ACCEPTED, challengeEntity.getAttempts().get(0).getStatus());
    }
    @Test
    public void testDeletingAChallengeWithAnAttemptAndThatTheAttemptIsDeletedToo() throws Exception {
        ChallengeEntity challengeEntity = challengeService.save(TestDataUtil.createChallengeEnitityA());
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT));
        challengeService.delete(challengeEntity.getId());
        assertFalse(challengeAttemptService.find(new ChallengeAttemptId(challengeEntity.getId(), "Test@Test.com")).isPresent());
    }
    @Test
    public void testAcceptingAnAlreadyAcceptedAttemptShouldGive409() throws Exception {
        ChallengeEntity challengeEntity = challengeService.save(TestDataUtil.createChallengeEnitityA());
        mockMvc.perform(post("/challenges/" + challengeEntity.getId() + "/submit/" + CONTENT));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/Test@Test.com"));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/Test@Test.com"))
                .andExpect(status().isConflict());
    }
}