package com.example.accessManager.service;

import com.example.accessManager.dto.*;
import com.example.accessManager.exceptions.NotFoundException;
import com.example.accessManager.wrapper.NewTeamDetailsWrapper;
import com.example.accessManager.wrapper.UpdateTeamAccessWrapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TeamService {
    List<TeamDTO> getAllTeams();

    TeamDTO addNewteam(NewTeamDetailsWrapper wrapper);


    List<TeamAccessControlDTO> updateTeamAccess(List<UpdateTeamAccessWrapper> wrapper);


    AccessControlDTO getTeamPermissions(Long id) throws NotFoundException;

    void deleteTeam(Long id) throws NotFoundException;

    List<AuditDTO> getAuditLogs(Long id) throws NotFoundException;

    List<TeamDTO> getTeamData(Long id) throws NotFoundException;

    List<AccessRequestDTO> getPendingRequests(UserDetails userDetails) throws NotFoundException;


    void saveRequestDecision(AccessRequestDTO accessRequestDTO) throws NotFoundException;

}
