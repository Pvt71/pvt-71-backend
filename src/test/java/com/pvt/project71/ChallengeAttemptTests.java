package com.pvt.project71;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvt.project71.domain.dto.ChallengeAttemptDto;
import com.pvt.project71.domain.entities.ChallengeAttemptEntity;
import com.pvt.project71.domain.entities.ChallengeAttemptId;
import com.pvt.project71.domain.enums.Status;
import com.pvt.project71.mappers.mapperimpl.ChallengeAttemptMapper;
import com.pvt.project71.mappers.mapperimpl.ChallengeMapper;
import com.pvt.project71.mappers.mapperimpl.UserMapperImpl;
import com.pvt.project71.repositories.ChallengeAttemptRepository;
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

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class ChallengeAttemptTests {
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


    @AfterEach
    public void cleanup() {challengeAttemptRepository.deleteAll();}
    //Mapper Tests
    @Test
    public void testCreatingEntityAndMapToDTOIsDoneCorrectly() {
        ChallengeAttemptEntity challengeAttemptEntity = ChallengeAttemptEntity.builder()
                .id(new ChallengeAttemptId(1, TestDataUtil.createValidTestUserEntity().getEmail()))
                //.challenge(TestDataUtil.createChallengeEnitityA())
                //.user(TestDataUtil.createValidTestUserEntity()).
                .status(Status.ACCEPTED).build();
        ChallengeAttemptDto challengeAttemptDto = challengeAttemptMapper.mapTo(challengeAttemptEntity);
        assertAll(
                () -> assertEquals(TestDataUtil.createValidTestUserEntity().getEmail(), challengeAttemptDto.getUserEmail()),
                () -> assertEquals(1, challengeAttemptDto.getChallengeId()),
                () -> assertEquals(challengeAttemptEntity.getStatus(), challengeAttemptDto.getStatus())
        );
    }
    @Test
    public void testCreatingDtoAndMapToEntityIsDoneCorrectly() {
        ChallengeAttemptDto challengeAttemptDto = ChallengeAttemptDto.builder().status(Status.ACCEPTED)
                .challengeId(TestDataUtil.createChallengeDtoA().getId())
                .userEmail(TestDataUtil.createValidTestUserDtoA().getEmail()).build();
        ChallengeAttemptEntity challengeAttemptEntity = challengeAttemptMapper.mapFrom(challengeAttemptDto);
        assertAll(
                //() -> assertEquals(challengeAttemptDto.getUser(), userMapper.mapTo(challengeAttemptEntity.getUser())),
                //() -> assertEquals(challengeAttemptDto.getChallenge(), challengeMapper.mapTo(challengeAttemptEntity.getChallenge())),
                () -> assertEquals(challengeAttemptDto.getStatus(), challengeAttemptEntity.getStatus()),
                () -> assertEquals(challengeAttemptDto.getUserEmail(), challengeAttemptEntity.getId().getUserEmail()),
                () -> assertEquals(challengeAttemptDto.getChallengeId(), challengeAttemptEntity.getId().getChallengeId())
        );
    }
}
