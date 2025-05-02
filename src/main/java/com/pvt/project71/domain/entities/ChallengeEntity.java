package com.pvt.project71.domain.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pvt.project71.domain.TimeStamps;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @ManyToOne()
    @JoinColumn(name = "creator_email", nullable = false)
    private UserEntity creator;

    private int rewardPoints;
    private String description;
}
