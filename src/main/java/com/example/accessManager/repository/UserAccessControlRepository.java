package com.example.accessManager.repository;

import com.example.accessManager.entity.UserAccessControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccessControlRepository extends JpaRepository<UserAccessControl, Long> {



    List<UserAccessControl> findAllByUser_IdInAndIsActiveTrue(List<Long> userId);

    List<UserAccessControl> findByUser_IdAndFeature_idIn(Long id, List<Long> featureIds);

    Optional<UserAccessControl> findByUser_IdAndFeature_Id(Long userId, Long featureId);
}
