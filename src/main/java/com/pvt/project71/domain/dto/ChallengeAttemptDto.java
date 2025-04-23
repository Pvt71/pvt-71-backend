package com.pvt.project71.domain.dto;

import com.pvt.project71.domain.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChallengeAttemptDto {
    //Integer challengeId;
    //String email;

    Status status;

    ChallengeDto challenge;
    UserDto user;
}
