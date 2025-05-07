package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.dto.ChallengeAttemptDto;
import com.pvt.project71.domain.entities.ChallengeAttemptEntity;
import com.pvt.project71.domain.entities.ChallengeAttemptId;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.mappers.mapperimpl.ChallengeAttemptMapper;
import com.pvt.project71.repositories.ChallengeAttemptRepository;
import com.pvt.project71.repositories.ChallengeRepository;
import com.pvt.project71.services.ChallengeAttemptService;
import com.pvt.project71.services.ChallengeService;
import com.pvt.project71.services.JwtService;
import com.pvt.project71.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    @Autowired
    private JwtService jwtService;

    @AfterEach
    public void cleanup() {challengeAttemptRepository.deleteAll(); challengeRepository.deleteAll();}
    @BeforeEach
    public void setUp() {
        fixAndSaveUser();
        userService.save(TestDataUtil.createValidTestUserEntityB());
    }

    private Jwt getUserToken() {
        return jwtService.mockOauth2(TestDataUtil.createValidTestUserEntity(),1, ChronoUnit.MINUTES);
    }
    private Jwt getOtherUserToken() {
        return jwtService.mockOauth2(TestDataUtil.createValidTestUserEntityB(),1, ChronoUnit.MINUTES);
    }
    private UserEntity fixAndSaveUser() {
        return userService.save(TestDataUtil.createValidTestUserEntity());
    }
    //Mapper Tests
    @Test
    public void testCreatingEntityAndMapToDTOIsDoneCorrectly() {
        ChallengeAttemptEntity challengeAttemptEntity = ChallengeAttemptEntity.builder()
                .id(new ChallengeAttemptId(1, TestDataUtil.createValidTestUserEntity().getEmail()))
                .status(Status.ACCEPTED).challenge(TestDataUtil.createChallengeEnitityA()).build();
        ChallengeAttemptDto challengeAttemptDto = challengeAttemptMapper.mapTo(challengeAttemptEntity);
        assertAll(
                () -> assertEquals(TestDataUtil.createValidTestUserEntity().getEmail(), challengeAttemptDto.getId().getUserEmail()),
                () -> assertEquals(1, challengeAttemptDto.getId().getChallengeId()),
                () -> assertEquals(challengeAttemptEntity.getStatus(), challengeAttemptDto.getStatus())
        );
    }
    @Test
    public void testCreatingDtoAndMapToEntityIsDoneCorrectly() {
        ChallengeAttemptDto challengeAttemptDto = ChallengeAttemptDto.builder().status(Status.ACCEPTED)
                .id(new ChallengeAttemptId(1, TestDataUtil.createValidTestUserDtoA().getEmail()))
                .build();
        ChallengeAttemptEntity challengeAttemptEntity = challengeAttemptMapper.mapFrom(challengeAttemptDto);
        assertAll(
                () -> assertEquals(challengeAttemptDto.getStatus(), challengeAttemptEntity.getStatus()),
                () -> assertEquals(challengeAttemptDto.getId().getUserEmail(), challengeAttemptEntity.getId().getUserEmail()),
                () -> assertEquals(challengeAttemptDto.getId().getChallengeId(), challengeAttemptEntity.getId().getChallengeId())
        );
    }

    @Test
    public void testSubmittingAnAttemptToExistingChallengeWorksCorrectly() throws Exception{
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT)
                        .with(jwt().jwt(getOtherUserToken())))
                .andExpect(status().isCreated())
                .andExpectAll(
                        jsonPath("$.status").value("PENDING"),
                        jsonPath("$.id.userEmail").value(getOtherUserToken().getSubject()),
                        jsonPath("$.id.challengeId").value(challengeEntity.getId()),
                        jsonPath("$.content").value(CONTENT));
    }
    @Test
    public void testSubmittingAnAttemptToANonExistingChallengeShouldGive404() throws Exception {
        mockMvc.perform(post("/challenges/" +1 +"/submit/" + CONTENT).with(jwt().jwt(getOtherUserToken())))
                .andExpect(status().isNotFound());
    }
    @Test
    public void testSubmittingAnAttemptCorrectlyAndCheckItIsSavedCorrectlyInRepository() throws Exception {
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT).with(jwt().jwt(getOtherUserToken())));
        assertTrue(challengeAttemptService.find(new ChallengeAttemptId(challengeEntity.getId(), getOtherUserToken().getSubject())).isPresent());
    }
    @Test
    public void testSubmittingAnAttemptToExistingChallengeWithAttemptFromUserAlreadyGives409() throws Exception {
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT).with(jwt().jwt(getOtherUserToken())));
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT + "littleDifferent").with(jwt().jwt(getOtherUserToken())))
                .andExpect(status().isConflict());
    }
    @Test
    @Transactional
    public void testSubmittingAnAttemptToExistingChallengeAndRetrieveItViaTheChallengeShouldGiveAttempt() throws Exception {
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT)
                .with(jwt().jwt(getOtherUserToken())));
        challengeEntity = challengeService.find(challengeEntity.getId()).get();
            assertFalse(challengeEntity.getAttempts().isEmpty());
    }
    @Test
    public void testAcceptingASubmittedAttempt() throws Exception{
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT).with(jwt().jwt(getOtherUserToken())));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/" + getOtherUserToken().getSubject())
                        .with(jwt().jwt(getUserToken())))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.status").value("ACCEPTED"),
                        jsonPath("$.id.userEmail").value(getOtherUserToken().getSubject()),
                        jsonPath("$.id.challengeId").value(challengeEntity.getId()),
                        jsonPath("$.content").value(CONTENT));
    }
    @Test
    public void testAcceptingAnUnsubmittedAttemptShouldGive404() throws Exception {
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/"+ getOtherUserToken().getSubject())
                        .with(jwt().jwt(getUserToken())))
                .andExpect(status().isNotFound());
    }
    @Test
    @Transactional
    public void testAcceptingSubmittedAttemptAndCheckItIsUpdatedInChallengesAttemptListCorrectly() throws Exception {
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT).with(jwt().jwt(getOtherUserToken())));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/"+ getOtherUserToken().getSubject())
                .with(jwt().jwt(getUserToken())));
        challengeEntity = challengeService.find(challengeEntity.getId()).get();
        assertEquals(Status.ACCEPTED, challengeEntity.getAttempts().get(0).getStatus());
    }
    @Test
    public void testDeletingAChallengeWithAnAttemptAndThatTheAttemptIsDeletedToo() throws Exception {
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT).with(jwt().jwt(getOtherUserToken())));
        challengeService.delete(challengeEntity.getId(), challengeEntity.getCreator());
        assertFalse(challengeAttemptService.find(new ChallengeAttemptId(challengeEntity.getId(), getOtherUserToken().getSubject())).isPresent());
    }
    @Test
    public void testAcceptingAnAlreadyAcceptedAttemptShouldGive409() throws Exception {
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" + challengeEntity.getId() + "/submit/" + CONTENT)
                .with(jwt().jwt(getOtherUserToken())));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/"+ getOtherUserToken().getSubject())
                .with(jwt().jwt(getUserToken())));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/"+ getOtherUserToken().getSubject())
                        .with(jwt().jwt(getUserToken())))
                .andExpect(status().isConflict());
    }
}