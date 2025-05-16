package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.ChallengeEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public interface ChallengeRepository extends CrudRepository<ChallengeEntity, Integer> {

    List<ChallengeEntity> findByCreatorEmail(String email);

    List<ChallengeEntity> findChallengeEntitiesByEvent_Id(Integer id);
    List<ChallengeEntity> findByCreatorEmailAndEventId(String email, Integer id);

    List<ChallengeEntity> findByEventName(String eventName);
    @Query("""
    SELECT c, ca FROM ChallengeEntity c
    LEFT JOIN ChallengeAttemptEntity ca ON ca.challenge.id = c.id AND ca.id.userEmail = :userEmail
    WHERE c.event.school = :school
    AND (c.dates.endsAt IS NULL OR c.dates.endsAt > CURRENT_TIMESTAMP)
    ORDER BY c.dates.updatedAt DESC
""")
    List<Object[]> findAllByEventSchool(String school, String userEmail);
}
