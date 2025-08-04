package com.example.accessManager.service.impl;

import com.example.accessManager.dto.AccessControlDTO;
import com.example.accessManager.dto.AuditDTO;
import com.example.accessManager.dto.TeamAccessControlDTO;
import com.example.accessManager.dto.TeamDTO;
import com.example.accessManager.entity.*;
import com.example.accessManager.enums.AccessMode;
import com.example.accessManager.enums.ActionType;
import com.example.accessManager.enums.EntityType;
import com.example.accessManager.exceptions.NotFoundException;
import com.example.accessManager.mapper.TeamMapper;
import com.example.accessManager.repository.*;
import com.example.accessManager.service.AuditTrailService;
import com.example.accessManager.service.TeamService;
import com.example.accessManager.service.UserService;
import com.example.accessManager.wrapper.*;
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
    private final FeatureRepository featureRepository;
    private final UserService userService;
    private final UserAccessControlRepository userAccessControlRepository;
    private final UserRepository userRepository;

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
        List<UpdateTeamAccessWrapper> updateTeamAccessWrapperList = new ArrayList<>();
        wrapper.getAccessList().forEach(x -> updateTeamAccessWrapperList.add(UpdateTeamAccessWrapper.builder().id(0L).teamId(team.getId()).featureId(x.getFeatureId()).hasAccess(x.isHasAccess()).build()));
        updateTeamAccess(updateTeamAccessWrapperList);
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

    @Override
    public void deleteTeam(Long id) throws NotFoundException {
        Team team = teamRepository.findById(id).orElseThrow(() -> new NotFoundException("Team not found with ID : {}" + id));
        team.setIsActive(false);
        List<User> usersList = new ArrayList<>(team.getUsers());
        teamRepository.save(team);
        auditTrailService.addAuditEntry(ActionType.UPDATE_TEAM,"Team Inactivated","",EntityType.TEAM,id);
        if(!usersList.isEmpty()){
            List<FeatureAccessWrapper> featureAccessWrappers= new ArrayList<>();
            List<Long> featureIds =  featureRepository.findAll().stream().map(Feature::getId).toList();
            featureIds.forEach(x -> featureAccessWrappers.add(FeatureAccessWrapper.builder().featureId(x).access(false).build()));
            usersList.forEach(x -> {
                if(x.getAccessMode().equals(AccessMode.INHERIT_TEAM_ACCESS)){
                    List<UserAccessControl> userAccessControls = userAccessControlRepository.findByUser_IdAndFeature_idIn(x.getId(),featureIds);
                    if(!userAccessControls.isEmpty()){
                        userAccessControls.forEach(u -> {
                            u.setUpdatedDate(new Date());
                            u.setIsActive(true);
                            u.setHasAccess(false);
                        });
                        userAccessControlRepository.saveAll(userAccessControls);
                        x.setAccessMode(AccessMode.OVERRIDE_TEAM_ACCESS);
                    }else{
                        FeatureAccessDetailsWrapper featureAccessDetailsWrapper = new FeatureAccessDetailsWrapper();
                        featureAccessDetailsWrapper.setFeatureAccessWrapperList(featureAccessWrappers);
                        UserAccessModeDetailsWrapper userAccessModeDetailsWrapper = new UserAccessModeDetailsWrapper();
                        userAccessModeDetailsWrapper.setUserId(x.getId());
                        userAccessModeDetailsWrapper.setAccessMode(AccessMode.OVERRIDE_TEAM_ACCESS.toString());
                        userAccessModeDetailsWrapper.setFeatureAccessDetailsWrapper(featureAccessDetailsWrapper);
                        try {
                            userService.updateAccessMode(userAccessModeDetailsWrapper);
                        } catch (NotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            });
            userRepository.saveAll(usersList);

        }

    }

    @Override
    public List<AuditDTO> getAuditLogs(Long id) throws NotFoundException {
        return auditTrailService.getAuditLogs(id,"Team");
    }
}
