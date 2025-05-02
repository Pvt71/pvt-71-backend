package com.pvt.project71.domain.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private String email;

    private String username;

    private String school;

    private String profilePictureUrl;

    @ManyToMany(mappedBy = "adminUsers", cascade = CascadeType.ALL)
    private List<EventEntity> events;

    //@OneToMany(mappedBy = "placeholder", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<Score> scores;

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ChallengeEntity> challenges;

}
