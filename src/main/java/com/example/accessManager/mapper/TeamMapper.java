package com.example.accessManager.mapper;

import com.example.accessManager.dto.TeamAccessControlDTO;
import com.example.accessManager.dto.TeamDTO;
import com.example.accessManager.entity.Feature;
import com.example.accessManager.entity.Team;
import com.example.accessManager.entity.TeamAccessControl;
import com.example.accessManager.wrapper.NewTeamDetailsWrapper;
import com.example.accessManager.wrapper.UpdateTeamAccessWrapper;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TeamMapper {
    public Team newTeamDetailsWrapperToTeam(NewTeamDetailsWrapper wrapper){
        return Team.builder()
                .name(wrapper.getName())
                .isActive(true)
                .createdDate(new Date())
                .build();
    }

    public TeamDTO teamToTeamDto(Team team){

        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .isActive(team.getIsActive())
                .build();
    }

    public TeamAccessControl AccessWrappertoAccessControl(UpdateTeamAccessWrapper wrapper){
        return TeamAccessControl.builder()
                .team(Team.builder()
                        .id(wrapper.getTeamId())
                        .build())
                .feature(Feature.builder()
                        .id(wrapper.getFeatureId())
                        .build())
                .hasAccess(wrapper.isHasAccess())
                .createdDate(new Date())
                .updatedDate(new Date())
                .build();
    }

    public TeamAccessControlDTO accessControlToDTO(TeamAccessControl teamAccessControl) {
        return TeamAccessControlDTO.builder()
                .id(teamAccessControl.getId())
                .teamId(teamAccessControl.getTeam().getId())
                .teamName(teamAccessControl.getTeam().getName())
                .featureId(teamAccessControl.getFeature().getId())
                .featureName(teamAccessControl.getFeature().getName())
                .hasAccess(teamAccessControl.isHasAccess())
                .build();
    }
}
