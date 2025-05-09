package com.pvt.project71;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pvt.project71.domain.TimeStamps;
import com.pvt.project71.domain.dto.ChallengeDto;
import com.pvt.project71.domain.dto.FriendshipDto;
import com.pvt.project71.domain.dto.UserDto;
import com.pvt.project71.domain.entities.*;
import com.pvt.project71.domain.enums.ProofType;
import com.pvt.project71.domain.dto.EventDto;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import com.pvt.project71.domain.entities.score.ScoreId;
import com.pvt.project71.domain.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TestDataUtil {
    private TestDataUtil() {}
    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2025, 10, 27, 16, 30, 0);
    public static TimeStamps createTimeStampForTest() {
        return TimeStamps.builder().endsAt(TEST_TIME)
                .build();
    }
    // "2025-10-27T16:30" test tiden som man kan ta och jämföra med.
    public static ChallengeEntity createChallengeEnitityA() {
        return  ChallengeEntity.builder().dates(createTimeStampForTest()).name("A").description("First Letter of the alphabet")
                .proofType(ProofType.REQUEST).rewardPoints(1000).build();
    }
    public static ChallengeDto createChallengeDtoA() {
        return  ChallengeDto.builder().name("A").description("First Letter of the alphabet")
                .dates(createTimeStampForTest()).proofType(ProofType.REQUEST).rewardPoints(1000).build();
    }
    public static ChallengeEntity createChallengeEnitityB() {
        return  ChallengeEntity.builder().name("B").description("Not the First Letter of the alphabet")
                .proofType(ProofType.REQUEST).dates(createTimeStampForTest()).rewardPoints(110).build();
    }
    public static ChallengeDto createChallengeDtoB() {
        return  ChallengeDto.builder().name("B").description("Not the First Letter of the alphabet")
                .proofType(ProofType.REQUEST).dates(createTimeStampForTest()).rewardPoints(110).build();
    }


    public static UserEntity createValidTestUserEntity(){
        return UserEntity.builder()
                .email("Test@test.com")
                .username("TestName")
                .school("TestSchool")
                .profilePictureUrl("testUrl")
                .build();
    }
    public static UserEntity createValidTestUserEntityB(){
        return UserEntity.builder()
                .email("TestB@test.com")
                .username("TestNameB")
                .school("TestSchoolB")
                .profilePictureUrl("testUrl")
                .build();
    }
    //event must be saved in the database as its primary key is auto gen
    //DO NOT forget to cleanup
    public static ScoreEntity createValidScoreEntity(UserEntity user, EventEntity event){
        return ScoreEntity.builder()
                .scoreId(ScoreId.builder().user(user).event(event).build())
                .score(100)
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
    public static EventEntity createTestEventEntityA() {
        return EventEntity.builder()
                .name("TestEventA")
                .description("TestDescription")
                .dates(createTimeStampForTest())
                .adminUsers(new ArrayList<>())
                .build();
    }
    public static EventDto createTestEventDtoA() {
        return EventDto.builder()
                .name("TestEventA")
                .description("TestDescription")
                .dates(createTimeStampForTest())
                .build();
    }
    public static EventEntity createTestEventEntityB() {
        return EventEntity.builder()
                .name("TestEventB")
                .description("TestDescription")
                .dates(createTimeStampForTest())
                .adminUsers(new ArrayList<>())
                .build();
    }
    public static EventDto createTestEventDtoB() {
        return EventDto.builder()
                .name("TestEventB")
                .description("TestDescription")
                .dates(createTimeStampForTest())
                .build();
    }

    public static FriendshipEntity createTestPendingFriendshipEntityA() {
        UserEntity userA = createValidTestUserEntity();
        UserEntity userB = createValidTestUserEntityB();

        return FriendshipEntity.builder().id(new FriendshipId(userA.getEmail(), userB.getEmail())).
                requester(userA).receiver(userB).status(Status.PENDING).build();
    }

    public static FriendshipDto createTestPendingFriendshipDtoA() {
        UserDto userA = createValidTestUserDtoA();
        UserDto userB = createValidTestUserDtoB();

        return FriendshipDto.builder().requester(userA).receiver(userB).status(Status.PENDING).build();
    }

}




