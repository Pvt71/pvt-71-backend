package com.pvt.project71;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pvt.project71.domain.dto.ChallengeDto;
import com.pvt.project71.domain.entities.ChallengeEntity;

import java.time.LocalDateTime;

public class TestDataUtil {
    private TestDataUtil() {}
    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2025, 10, 27, 16, 30, 0);
    // "2025-10-27T16:30" test tiden som man kan ta och jämföra med.
    public static ChallengeEntity createChallengeEnitityA() {
        return  ChallengeEntity.builder().endDate(TEST_TIME).name("A").description("First Letter of the alphabet")
                .rewardPoints(1000).build();
    }
    public static ChallengeDto createChallengeDtoA() {
        return  ChallengeDto.builder().endDate(TEST_TIME).name("A").description("First Letter of the alphabet")
                .rewardPoints(1000).build();
    }
    public static ChallengeEntity createChallengeEnitityB() {
        return  ChallengeEntity.builder().endDate(TEST_TIME).name("B").description("Not the First Letter of the alphabet")
                .rewardPoints(110).build();
    }
    public static ChallengeDto createChallengeDtoB() {
        return  ChallengeDto.builder().endDate(TEST_TIME).name("B").description("Not the First Letter of the alphabet")
                .rewardPoints(110).build();
    }
}
