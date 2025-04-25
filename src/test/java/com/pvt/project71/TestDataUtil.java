package com.pvt.project71;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pvt.project71.domain.dto.ChallengeDto;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.ChallengeAttemptEntity;
import com.pvt.project71.domain.entities.ChallengeEntity;
import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.enums.ProofType;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TestDataUtil {
    private TestDataUtil() {}
    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2025, 10, 27, 16, 30, 0);
    // "2025-10-27T16:30" test tiden som man kan ta och jämföra med.
    public static ChallengeEntity createChallengeEnitityA() {
        return  ChallengeEntity.builder().endDate(TEST_TIME).name("A").description("First Letter of the alphabet")
                .rewardPoints(1000).proofType(ProofType.REQUEST).build();
    }
    public static ChallengeDto createChallengeDtoA() {
        return  ChallengeDto.builder().endDate(TEST_TIME).name("A").description("First Letter of the alphabet")
                .rewardPoints(1000).proofType(ProofType.REQUEST).build();
    }
    public static ChallengeEntity createChallengeEnitityB() {
        return  ChallengeEntity.builder().endDate(TEST_TIME).name("B").description("Not the First Letter of the alphabet")
                .rewardPoints(110).build();
    }
    public static ChallengeDto createChallengeDtoB() {
        return  ChallengeDto.builder().endDate(TEST_TIME).name("B").description("Not the First Letter of the alphabet")
                .rewardPoints(110).build();
    }


    public static UserEntity createValidTestUserEntity(){
        return UserEntity.builder()
                .email("Test@test.com")
                .username("TestName")
                .school("TestSchool")
                .profilePictureUrl("testUrl")
                .build();
    }

    public static UserEntity createInvalidTestUserEntity(){
        return UserEntity.builder()
                .email("")
                .username("TestName")
                .school("TestSchool")
                .profilePictureUrl("testUrl")
                .build();
    }

    public static UserDto createValidTestUserDtoA(){
        return UserDto.builder()
                .email("Test@test.com")
                .username("TestName")
                .school("TestSchool")
                .profilePictureUrl("testUrl")
                .build();
    }

    public static UserDto createValidTestUserDtoB(){
        return UserDto.builder()
                .email("Test2@test.com")
                .username("TestName2")
                .school("TestSchool2")
                .profilePictureUrl("testUrl2")
                .build();
    }

    public static UserDto createTestUserDtoBlankEmail(){
        return UserDto.builder()
                .email("")
                .username("TestName2")
                .school("TestSchool2")
                .profilePictureUrl("testUrl2")
                .build();
    }

}
