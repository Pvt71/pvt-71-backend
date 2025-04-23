package com.pvt.project71.domain.entities;

import com.pvt.project71.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="challengeAttempts")
public class ChallengeAttemptEntity {
    @EmbeddedId
    ChallengeAttemptId id;

    Status status;


    @ManyToOne
    @MapsId("challengeId")
    ChallengeEntity challenge;

    @ManyToOne
    @MapsId("email")
    UserEntity user;
}
