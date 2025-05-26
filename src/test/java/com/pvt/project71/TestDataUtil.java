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
                .proofType(ProofType.REQUEST).points(99).build();
    }
    public static ChallengeDto createChallengeDtoA() {
        return  ChallengeDto.builder().name("A").description("First Letter of the alphabet")
                .dates(createTimeStampForTest()).proofType(ProofType.REQUEST).points(99).build();
    }
    public static ChallengeEntity createChallengeEnitityB() {
        return  ChallengeEntity.builder().name("B").description("Not the First Letter of the alphabet")
                .proofType(ProofType.REQUEST).dates(createTimeStampForTest()).points(98).build();
    }
    public static ChallengeDto createChallengeDtoB() {
        return  ChallengeDto.builder().name("B").description("Not the First Letter of the alphabet")
                .proofType(ProofType.REQUEST).dates(createTimeStampForTest()).points(98).build();
    }
    public static final String SCHOOL_NAME = "TestSchool";

    public static UserEntity createValidTestUserEntity(){
        return UserEntity.builder()
                .email("Test@test.com")
                .username("TestName")
                .school(SCHOOL_NAME)
                .profilePicture(createTestImageBytes())
                .build();
    }
    public static UserEntity createValidTestUserEntityB(){
        return UserEntity.builder()
                .email("TestB@test.com")
                .username("TestNameB")
                .school("Another school name")
                .profilePicture(createTestImageBytes())
                .build();
    }
    public static UserEntity createValidTestUserEntityC(){
        return UserEntity.builder()
                .email("TestC@test.com")
                .username("TestNameC")
                .school("TestSchoolC")
                .profilePicture(createTestImageBytes())
                .build();
    }
    public static UserEntity createValidTestUserEntityD(){
        return UserEntity.builder()
                .email("TestD@test.com")
                .username("TestNameD")
                .school("TestSchoolD")
                .profilePicture(createTestImageBytes())
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
                .school(SCHOOL_NAME)
                .profilePicture(createTestImageBytes())
                .build();
    }

    public static UserDto createValidTestUserDtoA(){
        return UserDto.builder()
                .email("Test@test.com")
                .username("TestName")
                .school(SCHOOL_NAME)
                .profilePictureUrl("/uploads/users/Test@test.com/profilePicture")
                .build();
    }

    public static UserDto createValidTestUserDtoB(){
        return UserDto.builder()
                .email("Test2@test.com")
                .username("TestName2")
                .school(SCHOOL_NAME)
                .profilePictureUrl("/uploads/users/Test@test.com/profilePicture")
                .build();
    }

    public static UserDto createTestUserDtoBlankEmail(){
        return UserDto.builder()
                .email("")
                .username("TestName2")
                .school(SCHOOL_NAME)
                .profilePictureUrl("/uploads/users/Test@test.com/profilePicture")
                .build();
    }
    public static EventEntity createTestEventEntityA() {
        return EventEntity.builder()
                .name("TestEventA")
                .description("TestDescription")
                .dates(createTimeStampForTest())
                .adminUsers(new ArrayList<>())
                .school(SCHOOL_NAME)
                .build();
    }
    public static EventDto createTestEventDtoA() {
        return EventDto.builder()
                .name("TestEventA")
                .description("TestDescription")
                .dates(createTimeStampForTest())
                .school(SCHOOL_NAME)
                .build();
    }
    public static EventEntity createTestEventEntityB() {
        return EventEntity.builder()
                .name("TestEventB")
                .description("TestDescription")
                .dates(createTimeStampForTest())
                .adminUsers(new ArrayList<>())
                .school(SCHOOL_NAME)
                .build();
    }
    public static EventDto createTestEventDtoB() {
        return EventDto.builder()
                .name("TestEventB")
                .description("TestDescription")
                .school(SCHOOL_NAME)
                .dates(createTimeStampForTest())
                .build();
    }

    public static byte[] createTestImageBytes() {
        // Just some sample bytes to represent an image
        return new byte[] {
                (byte) 0xFF, (byte) 0xD8, // JPEG SOI marker
                0x00, 0x10, 0x20, 0x30,   // arbitrary filler bytes
                (byte) 0xFF, (byte) 0xD9  // JPEG EOI marker
        };
    }

    public static FriendshipEntity createTestPendingFriendshipEntityA() {
        UserEntity userA = createValidTestUserEntity();
        UserEntity userB = createValidTestUserEntityB();

        return FriendshipEntity.builder().id(new FriendshipId(userA.getEmail(), userB.getEmail())).
                requester(userA).receiver(userB).status(Status.PENDING).build();
    }

    public static FriendshipEntity createTestPendingFriendshipEntityB() {
        UserEntity userA = createValidTestUserEntity();
        UserEntity userC = createValidTestUserEntityC();

        return FriendshipEntity.builder().id(new FriendshipId(userA.getEmail(), userC.getEmail())).
                requester(userC).receiver(userA).status(Status.PENDING).build();
    }

    public static FriendshipEntity createTestAcceptedFriendshipEntityA() {
        UserEntity userA = createValidTestUserEntity();
        UserEntity userD = createValidTestUserEntityD();

        return FriendshipEntity.builder().id(new FriendshipId(userA.getEmail(), userD.getEmail())).
                requester(userA).receiver(userD).status(Status.ACCEPTED).build();
    }

    public static FriendshipEntity createTestAcceptedFriendshipEntityB() {
        UserEntity userA = createValidTestUserEntity();
        UserEntity userB = createValidTestUserEntityB();

        return FriendshipEntity.builder().id(new FriendshipId(userA.getEmail(), userB.getEmail())).
                requester(userA).receiver(userB).status(Status.ACCEPTED).build();
    }

    public static FriendshipDto createTestPendingFriendshipDtoA() {
        UserDto userA = createValidTestUserDtoA();
        UserDto userB = createValidTestUserDtoB();

        return FriendshipDto.builder().requester(userA).receiver(userB).status(Status.PENDING).build();
    }

}




