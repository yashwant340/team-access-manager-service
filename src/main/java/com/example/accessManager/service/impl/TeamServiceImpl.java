package com.example.accessManager.service.impl;

import com.example.accessManager.dto.AccessControlDTO;
import com.example.accessManager.dto.TeamAccessControlDTO;
import com.example.accessManager.dto.TeamDTO;
import com.example.accessManager.entity.Team;
import com.example.accessManager.entity.TeamAccessControl;
import com.example.accessManager.enums.ActionType;
import com.example.accessManager.enums.EntityType;
import com.example.accessManager.exceptions.NotFoundException;
import com.example.accessManager.mapper.TeamMapper;
import com.example.accessManager.repository.TeamAccessControlRepository;
import com.example.accessManager.repository.TeamRepository;
import com.example.accessManager.service.AuditTrailService;
import com.example.accessManager.service.TeamService;
import com.example.accessManager.wrapper.NewTeamDetailsWrapper;
import com.example.accessManager.wrapper.UpdateTeamAccessWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final TeamAccessControlRepository teamAccessControlRepository;
    private final AuditTrailService auditTrailService;

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
        auditTrailService.addAuditEntry(ActionType.ADD_TEAM,"New Team Added","", EntityType.TEAM,team.getId());
        return teamMapper.teamToTeamDto(team);
    }

    @Override
    public List<TeamAccessControlDTO> updateTeamAccess(List<UpdateTeamAccessWrapper> wrapper) {
        List<TeamAccessControlDTO> teamAccessControlDTOS = new ArrayList<>();
        for(UpdateTeamAccessWrapper accessWrapper : wrapper){
            TeamAccessControl teamAccessControl = new TeamAccessControl();
            if(accessWrapper.getId() != 0){
                Optional<TeamAccessControl> teamAccess = teamAccessControlRepository.findById(accessWrapper.getId());
                if(teamAccess.isPresent()){
                    TeamAccessControl existingTeamAccess = teamAccess.get();
                    existingTeamAccess.setHasAccess(accessWrapper.isHasAccess());
                    existingTeamAccess.setUpdatedDate(new Date());
                    teamAccessControl = teamAccessControlRepository.save(existingTeamAccess);
                    auditTrailService.addAuditEntry(ActionType.TEAM_ACCESS_CHANGE, teamAccessControl.isHasAccess() ? "Access provisioned" : "Access revoked", "",EntityType.TEAM_ACCESS, teamAccessControl.getId() );
                }

            } else {
                teamAccessControl= teamAccessControlRepository.save(teamMapper.AccessWrappertoAccessControl(accessWrapper));
                auditTrailService.addAuditEntry(ActionType.TEAM_ACCESS_CHANGE, teamAccessControl.isHasAccess() ? "Access provisioned" : "Access not Provisioned","",EntityType.TEAM_ACCESS, teamAccessControl.getId());
            }

            teamAccessControlDTOS.add(teamMapper.accessControlToDTO(teamAccessControl));
        }


        return teamAccessControlDTOS ;
    }

    @Override
    public AccessControlDTO getTeamPermissions(Long id) throws NotFoundException {
        Team team = teamRepository.findById(id).orElseThrow(() -> new NotFoundException("Team not found with ID : {}" + id));
        List<TeamAccessControl> accessControls = team.getAccessControls();
        List<TeamAccessControlDTO> teamAccessControlDTOS = new ArrayList<>();
        for(TeamAccessControl access:accessControls) {
            teamAccessControlDTOS.add(teamMapper.accessControlToDTO(access));
        }
        AccessControlDTO accessControlDTO = new AccessControlDTO();
        accessControlDTO.setTeamAccessControlDTOS(teamAccessControlDTOS);
        return accessControlDTO;
    }
}
