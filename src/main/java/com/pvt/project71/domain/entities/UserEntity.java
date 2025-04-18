package com.pvt.project71.domain.entities;


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
@Table(name = "users")
public class UserEntity {

    @Id
    private String email;

    private String username;

    private String school;

    private String profilePictureUrl;

    //När events/scores/challenges finns:
    //@OneToMany(mappedBy = "placeholder", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<Event> events;

    //@OneToMany(mappedBy = "placeholder", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<Score> scores;

    //@OneToMany(mappedBy = "placeholder", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<Challenge> challenges;

}
