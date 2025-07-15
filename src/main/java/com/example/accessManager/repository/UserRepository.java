package com.example.accessManager.repository;

import com.example.accessManager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {


    List<User> findAllByIsActiveTrue();
}
