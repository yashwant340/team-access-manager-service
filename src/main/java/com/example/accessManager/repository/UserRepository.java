package com.example.accessManager.repository;

import com.example.accessManager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    List<User> findAllByIsActiveTrue();

    Optional<User> findByUsername(String username);
}
