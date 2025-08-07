package com.example.accessManager.repository;

import com.example.accessManager.entity.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
    @Query(value = """
            SELECT * FROM (
              SELECT a.*
              FROM audit_trail a
              INNER JOIN team_access_control t ON a.team_access_control_id = t.id
              WHERE t.team_id = (:teamId)
            
              UNION
            
              SELECT a.*
              FROM audit_trail a
              WHERE a.team_id = (:teamId)
            ) AS audit
            ORDER BY audit.updated_date DESC;
            """, nativeQuery = true)
    List<AuditTrail> findAllByTeam(@Param("teamId") Long teamId);

    @Query(value = """
            SELECT * FROM (
              SELECT a.*
              FROM audit_trail a
              INNER JOIN user_access_control u ON a.user_access_control_id = u.id
              WHERE u.user_id = (:userId)
            
              UNION
            
              SELECT a.*
              FROM audit_trail a
              WHERE a.user_id = (:userId)
            ) AS audit
            ORDER BY audit.updated_date DESC;
            """, nativeQuery = true)
    List<AuditTrail> findAllByUser(@Param("userId") Long userId);}
