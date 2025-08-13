package com.example.accessManager.repository;

import com.example.accessManager.entity.LoginRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginRequestRepository extends JpaRepository<LoginRequest, Long> {
    List<LoginRequest> findAllByIsActiveTrue();
}
