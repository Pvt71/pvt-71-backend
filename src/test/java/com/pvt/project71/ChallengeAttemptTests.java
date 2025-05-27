package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.dto.ChallengeAttemptDto;
import com.pvt.project71.domain.entities.*;
import com.pvt.project71.domain.entities.score.ScoreId;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.mappers.mapperimpl.ChallengeAttemptMapper;
import com.pvt.project71.repositories.ChallengeAttemptRepository;
import com.pvt.project71.repositories.ChallengeRepository;
import com.pvt.project71.repositories.ScoreRepository;
import com.pvt.project71.services.*;
import com.pvt.project71.services.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
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
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private EventService eventService;
    @Autowired
    ScoreRepository scoreRepository;

    @AfterEach
    public void cleanup() {challengeAttemptRepository.deleteAll(); challengeRepository.deleteAll(); scoreRepository.deleteAll();}
    @BeforeEach
    public void setUp() {
        fixAndSaveUser();
        userService.save(TestDataUtil.createValidTestUserEntityB());
    }

    private Jwt getUserToken() {
        return jwtService.generateTokenFromUserEntity(TestDataUtil.createValidTestUserEntity(),1, ChronoUnit.MINUTES);
    }
    private Jwt getOtherUserToken() {
        return jwtService.generateTokenFromUserEntity(TestDataUtil.createValidTestUserEntityB(),1, ChronoUnit.MINUTES);
    }
    private UserEntity fixAndSaveUser() {
        return userService.save(TestDataUtil.createValidTestUserEntity());
    }
    //Mapper Tests
    @Test
    public void testCreatingEntityAndMapToDTOIsDoneCorrectly() {
        ChallengeAttemptEntity challengeAttemptEntity = ChallengeAttemptEntity.builder()
                .id(new ChallengeAttemptId(1, TestDataUtil.createValidTestUserEntity().getEmail()))
                .status(Status.ACCEPTED).challenge(TestDataUtil.createChallengeEnitityA()).username("A").build();
        ChallengeAttemptDto challengeAttemptDto = challengeAttemptMapper.mapTo(challengeAttemptEntity);
        assertAll(
                () -> assertEquals(TestDataUtil.createValidTestUserEntity().getEmail(), challengeAttemptDto.getId().getUserEmail()),
                () -> assertEquals(1, challengeAttemptDto.getId().getChallengeId()),
                () -> assertEquals(challengeAttemptEntity.getStatus(), challengeAttemptDto.getStatus()),
                () -> assertEquals(challengeAttemptEntity.getUsername(), challengeAttemptDto.getUsername())
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
                        jsonPath("$.content").value(CONTENT),
                        jsonPath("$.username").value(TestDataUtil.createValidTestUserEntityB().getUsername()));
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
    public void testSubmittingAttemptToYourOwnCreatedChallengesGives403() throws Exception {
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT)
                .with(jwt().jwt(getUserToken()))).andExpect(status().isForbidden());
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
    public void testAcceptingASubmittedAttemptwhenMaxCompletionIsReachedGives409() throws Exception{
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        challengeEntity.setMaxCompletions(1);
        challengeEntity.setCompletionCount(1);
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT).with(jwt().jwt(getOtherUserToken())));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/" + getOtherUserToken().getSubject())
                        .with(jwt().jwt(getUserToken())))
                .andExpect(status().isConflict());
    }
    @Test
    public void testRejectingASubmittedAttempt() throws Exception{
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT).with(jwt().jwt(getOtherUserToken())));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/reject/" + getOtherUserToken().getSubject())
                        .with(jwt().jwt(getUserToken())))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.status").value("REJECTED"),
                        jsonPath("$.id.userEmail").value(getOtherUserToken().getSubject()),
                        jsonPath("$.id.challengeId").value(challengeEntity.getId()),
                        jsonPath("$.content").value(""));
    }
    @Test
    public void testAttemptingARejectedAttemptAgain() throws Exception {
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT).with(jwt().jwt(getOtherUserToken())));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/reject/" + getOtherUserToken().getSubject())
                .with(jwt().jwt(getUserToken())));
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT).with(jwt().jwt(getOtherUserToken())))
                .andExpect(status().isCreated())
                .andExpectAll(
                        jsonPath("$.status").value("PENDING"),
                        jsonPath("$.id.userEmail").value(getOtherUserToken().getSubject()),
                        jsonPath("$.id.challengeId").value(challengeEntity.getId()),
                        jsonPath("$.content").value(CONTENT));
    }
    @Test
    public void testAttemptingARejectedAttemptAgainUpdatesInChallenges() throws Exception {
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT).with(jwt().jwt(getOtherUserToken())));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/reject/" + getOtherUserToken().getSubject())
                .with(jwt().jwt(getUserToken())));
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT).with(jwt().jwt(getOtherUserToken())));
        challengeEntity = challengeService.find(challengeEntity.getId()).get();
        assertEquals(Status.PENDING, challengeEntity.getAttempts().get(0).getStatus());

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
    public void testAcceptingSubmittedAttemptGivesUserItsPoints() throws Exception {
        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        UserEntity user = fixAndSaveUser();
        challengeEntity.setCreator(user);
        challengeEntity = challengeService.save(challengeEntity, user);
        mockMvc.perform(post("/challenges/" +challengeEntity.getId() +"/submit/" + CONTENT).with(jwt().jwt(getOtherUserToken())));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/"+ getOtherUserToken().getSubject())
                .with(jwt().jwt(getUserToken())));
        assertEquals(challengeEntity.getPoints(),scoreService.findOne(ScoreId.builder().event(eventService.getDefaultEvent(TestDataUtil.SCHOOL_NAME))
                .user(userService.findOne(getOtherUserToken().getSubject()).get()).build()).get().getScore());
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
        mockMvc.perform(post("/challenges/" + challengeEntity.getId() + "/submit")
                .with(jwt().jwt(getOtherUserToken())));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/"+ getOtherUserToken().getSubject())
                .with(jwt().jwt(getUserToken())));
        mockMvc.perform(patch("/challenges/" + challengeEntity.getId() + "/accept/"+ getOtherUserToken().getSubject())
                        .with(jwt().jwt(getUserToken())))
                .andExpect(status().isConflict());
    }
    @Test
    public void testRetrievingAlistOfChallengesUserCanReviewIsCorrectlySized() throws Exception {
        UserEntity user = fixAndSaveUser();

        EventEntity eventEntity = TestDataUtil.createTestEventEntityA();
        user = userService.loadTheLazy(user);
        user.getEvents().add(eventEntity);
        eventEntity.getAdminUsers().add(user);
        eventEntity = eventService.save(eventEntity, user);

        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        challengeEntity.setCreator(user);
        challengeService.save(challengeEntity, user);

        ChallengeEntity challengeEntityB = TestDataUtil.createChallengeEnitityB();

        challengeEntityB.setCreator(user);
        challengeEntityB.setEvent(eventEntity);
        challengeService.save(challengeEntityB, user);

        UserEntity userB = TestDataUtil.createValidTestUserEntity();
        userB.setEmail("test2@test.com");
        userB = userService.save(userB);
        userB = userService.makeAdmin(userB, eventEntity, user);
        eventEntity = eventService.findOne(eventEntity.getId()).get();

        ChallengeEntity challengeEntityC = TestDataUtil.createChallengeEnitityB();
        challengeEntityC.setName("TESTC");
        challengeEntityC.setEvent(eventEntity);
        challengeEntityC.setCreator(userB);
        challengeService.save(challengeEntityC, userB);

        UserEntity userC = UserEntity.builder().email("GOOB@Goob.com").school("Unemployed").username("Coolguy")
                .build();
        userC = userService.save(userC);
        Jwt userCJwt = jwtService.generateTokenFromUserEntity(userC, 1, ChronoUnit.MINUTES);
        mockMvc.perform(post("/challenges/" + challengeEntity.getId() + "/submit/" + CONTENT)
                .with(jwt().jwt(userCJwt)));
        mockMvc.perform(post("/challenges/" + challengeEntityB.getId() + "/submit/" + CONTENT)
                .with(jwt().jwt(userCJwt)));
        mockMvc.perform(post("/challenges/" + challengeEntityC.getId() + "/submit/" + CONTENT)
                .with(jwt().jwt(userCJwt)));

        mockMvc.perform(get("/challenges/pending")
                .with(jwt().jwt(getUserToken())))
                .andExpect(jsonPath("$[2]").exists())
                .andExpect(jsonPath("$[3]").doesNotExist());

    }
    @Test
    public void testAcceptingAcceptingYourOwnChallengeAttemptInAnEventYouAreAdminInGives403() throws Exception {
        UserEntity user = fixAndSaveUser();
        EventEntity eventEntity = TestDataUtil.createTestEventEntityA();
        user = userService.loadTheLazy(user);
        user.getEvents().add(eventEntity);
        eventEntity.getAdminUsers().add(user);
        eventEntity = eventService.save(eventEntity, user);

        UserEntity userB = TestDataUtil.createValidTestUserEntity();
        userB.setEmail("test2@test.com");
        userB = userService.save(userB);
        userB = userService.makeAdmin(userB, eventEntity, user);
        eventEntity = eventService.findOne(eventEntity.getId()).get();

        ChallengeEntity challengeEntity = TestDataUtil.createChallengeEnitityA();
        challengeEntity.setCreator(user);
        challengeService.save(challengeEntity, user);
        Jwt userBJwt = jwtService.generateTokenFromUserEntity(userB, 1, ChronoUnit.MINUTES);
        mockMvc.perform(post("/challenges/" + challengeEntity.getId() + "/submit/" + CONTENT)
                .with(jwt().jwt(userBJwt)));
        mockMvc.perform(patch("/challenges/"+ challengeEntity.getId() + "/accept/"+userB.getEmail())
                        .with(jwt().jwt(userBJwt))).andExpect(status().isForbidden());
    }
}