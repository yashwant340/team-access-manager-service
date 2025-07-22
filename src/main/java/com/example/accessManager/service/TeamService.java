package com.example.accessManager.service;

import com.example.accessManager.dto.AccessControlDTO;
import com.example.accessManager.dto.TeamAccessControlDTO;
import com.example.accessManager.dto.TeamDTO;
import com.example.accessManager.dto.UserDTO;
import com.example.accessManager.exceptions.NotFoundException;
import com.example.accessManager.wrapper.NewTeamDetailsWrapper;
import com.example.accessManager.wrapper.UpdateTeamAccessWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TeamService {
    List<TeamDTO> getAllTeams();

    TeamDTO addNewteam(NewTeamDetailsWrapper wrapper);


    List<TeamAccessControlDTO> updateTeamAccess(List<UpdateTeamAccessWrapper> wrapper);


    AccessControlDTO getTeamPermissions(Long id) throws NotFoundException;
}
