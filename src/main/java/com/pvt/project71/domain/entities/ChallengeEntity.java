package com.pvt.project71.domain.entities;

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
@Table(name="challenges")
public class ChallengeEntity {

    @Id
    @GeneratedValue(generator = "IDENTITY")
    private Integer id;

    private LocalDateTime endDate;
    private String name;

    //private EventEntity event;
    //private UserEntity creator;

    private int rewardPoints;
    private String description;
}
