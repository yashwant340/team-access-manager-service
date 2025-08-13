package com.example.accessManager.repository;

import com.example.accessManager.entity.TeamAccessControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamAccessControlRepository extends JpaRepository<TeamAccessControl, Long> {

    List<TeamAccessControl> findAllByTeam_Id(Long teamId);
}
