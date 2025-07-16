package com.example.accessManager.service;

import com.example.accessManager.dto.TeamDTO;
import com.example.accessManager.dto.UserDTO;
import com.example.accessManager.wrapper.NewTeamDetailsWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TeamService {
    List<TeamDTO> getAllTeams();

    TeamDTO addNewteam(NewTeamDetailsWrapper wrapper);
}
