package com.example.accessManager.service;

import com.example.accessManager.dto.AccessControlDTO;
import com.example.accessManager.dto.AuditDTO;
import com.example.accessManager.dto.UserDTO;
import com.example.accessManager.exceptions.NotFoundException;
import com.example.accessManager.wrapper.UserAccessModeDetailsWrapper;
import com.example.accessManager.wrapper.UserDetailsWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    List<UserDTO> getAllUsers();

    AccessControlDTO getUserPermissions(Long id) throws NotFoundException;

    UserDTO addNewUser(UserDetailsWrapper wrapper);

    UserDTO updateUser(UserDetailsWrapper wrapper);

    void updateAccessMode(UserAccessModeDetailsWrapper wrapper) throws NotFoundException;

    List<AuditDTO> getAuditLogs(Long id) throws NotFoundException;

    UserDTO deleteUser(Long id) throws NotFoundException;
}
