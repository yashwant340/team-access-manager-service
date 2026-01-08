package com.example.accessManager.service;

import com.example.accessManager.dto.LoginRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AdminService {
    List<LoginRequestDTO> getAllLoginRequests();

    void approveRequest(Long id, Long teamId);
}
