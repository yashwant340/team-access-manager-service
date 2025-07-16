package com.example.accessManager.mapper;

import com.example.accessManager.dto.TeamDTO;
import com.example.accessManager.entity.Team;
import com.example.accessManager.wrapper.NewTeamDetailsWrapper;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {
    public Team newTeamDetailsWrapperToTeam(NewTeamDetailsWrapper wrapper){
        return Team.builder()
                .name(wrapper.getName())
                .build();
    }

    public TeamDTO teamToTeamDto(Team team){
        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .isActive(team.getIsActive())
                .build();
    }
}
