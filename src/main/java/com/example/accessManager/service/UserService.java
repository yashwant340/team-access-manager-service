package com.example.accessManager.service;

import com.example.accessManager.dto.*;
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

    void updateAccessMode(UserAccessModeDetailsWrapper wrapper, boolean isAuditRequired) throws NotFoundException;

    List<AuditDTO> getAuditLogs(Long id) throws NotFoundException;

    UserDTO deleteUser(Long id) throws NotFoundException;

    List<UserDTO> getAllUsersOfTeam(Long id);

    List<UserDashboardAccessDataDTO> getAccessData(Long id) throws NotFoundException;

    void saveAccessRequest(AccessRequestDTO accessRequestDTO) throws NotFoundException;

    UserDTO getUser(Long id) throws NotFoundException;

    List<AuditDTO> getUserDashboardAudit(Long id) throws NotFoundException;
}
