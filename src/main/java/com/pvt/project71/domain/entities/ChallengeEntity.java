package com.pvt.project71.domain.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pvt.project71.domain.enums.ProofType;
import com.pvt.project71.domain.TimeStamps;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="challenges")
public class ChallengeEntity {

    @Id
    @GeneratedValue(generator = "IDENTITY")
    private Integer id;

    @Embedded
    private TimeStamps dates;

    private String name;
    private Integer maxCompletions;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_email", nullable = false)
    private UserEntity creator;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<ChallengeAttemptEntity> attempts;


    private int rewardPoints;
    private String description;
    private ProofType proofType;

}
