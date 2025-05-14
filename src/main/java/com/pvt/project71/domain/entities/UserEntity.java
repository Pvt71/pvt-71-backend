package com.pvt.project71.domain.entities;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private String password;
    @Lob
    @Column(name = "profile_picture", columnDefinition = "LONGBLOB")
    private byte[] profilePicture;

    @ManyToMany(mappedBy = "adminUsers", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<EventEntity> events;



    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ChallengeEntity> challenges;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity)) return false;
        UserEntity other = (UserEntity) o;
        return other.getEmail().equals(email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
