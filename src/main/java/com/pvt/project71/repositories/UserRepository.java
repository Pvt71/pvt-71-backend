package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.UserEntity;
import com.pvt.project71.domain.entities.score.ScoreEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, String> {
    @Query("""
        SELECT DISTINCT u.school FROM UserEntity u
        WHERE u.email IS NOT NULL
        ORDER BY u.school DESC
    """)
    List<String> findAllSchools();

    @Query("""
    SELECT s FROM ScoreEntity s
    WHERE EXISTS (
            SELECT u.school FROM UserEntity u WHERE u.email = :email
                AND u = s.scoreId.user
            )
    AND s.scoreId.event.isDefault = true
    """)
    Optional<ScoreEntity> findScoresFromUserSchool(String email);
}