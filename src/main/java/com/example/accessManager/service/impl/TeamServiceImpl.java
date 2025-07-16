package com.example.accessManager.service.impl;

import com.example.accessManager.dto.TeamDTO;
import com.example.accessManager.entity.Team;
import com.example.accessManager.mapper.TeamMapper;
import com.example.accessManager.repository.TeamRepository;
import com.example.accessManager.service.TeamService;
import com.example.accessManager.wrapper.NewTeamDetailsWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    @Override
    public List<TeamDTO> getAllTeams() {
        List<Team> teamList = teamRepository.findAllByIsActiveTrue();
        List<TeamDTO> teamDTOList = new ArrayList<>();
        teamList.forEach(x -> teamDTOList.add(teamMapper.teamToTeamDto(x)));
        return teamDTOList;
    }

    @Override
    public TeamDTO addNewteam(NewTeamDetailsWrapper wrapper) {
        Team team = teamRepository.save(teamMapper.newTeamDetailsWrapperToTeam(wrapper));
        return teamMapper.teamToTeamDto(team);
    }
}
