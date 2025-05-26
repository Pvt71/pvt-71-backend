package com.pvt.project71.repositories;

import com.pvt.project71.domain.entities.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, String> {
    @Query("""
        SELECT DISTINCT u.school FROM UserEntity u
        WHERE u.email IS NOT NULL
        ORDER BY u.school DESC
    """)
    List<String> findAllSchools();
}