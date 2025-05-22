package com.pvt.project71.domain.entities;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pvt.project71.domain.entities.score.ScoreEntity;
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

    private boolean newNotifications;

    @Lob
    @Column(name = "profile_picture", columnDefinition = "LONGBLOB")
    private byte[] profilePicture;

    @Lob
    @Column(name = "profile_picture_thumbnail", columnDefinition = "LONGBLOB")
    private byte[] profilePictureThumbnail;

    @ManyToMany(mappedBy = "adminUsers", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<EventEntity> events;

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ChallengeEntity> challenges;

    @OneToMany(mappedBy = "scoreId.user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<ScoreEntity> scores;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<BadgeEntity> badges;

    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<NotificationEntity> notifications;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity)) return false;
        UserEntity other = (UserEntity) o;
        return other.getEmail().trim().equalsIgnoreCase(email.trim());
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
