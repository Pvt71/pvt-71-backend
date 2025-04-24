package com.pvt.project71.domain.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pvt.project71.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    LocalDateTime submittedAt;
    String content;
    //@ManyToOne
    //@MapsId("email")
    //UserEntity user;
}
