package com.example.accessManager.repository;

import com.example.accessManager.entity.AccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {

    List<AccessRequest> findAllByUser_IdIn(List<Long> usersId);

    AccessRequest findByIdAndIsActiveTrue(Long id);

    List<AccessRequest> findAllByUser_IdAndIsActiveTrue(Long id);
}
