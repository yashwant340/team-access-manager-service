package com.example.accessManager.service.impl;

import com.example.accessManager.dto.*;
import com.example.accessManager.entity.*;
import com.example.accessManager.enums.AccessMode;
import com.example.accessManager.enums.ActionType;
import com.example.accessManager.enums.EntityType;
import com.example.accessManager.enums.PendingRequestStatus;
import com.example.accessManager.exceptions.NotFoundException;
import com.example.accessManager.mapper.TeamMapper;
import com.example.accessManager.mapper.UserMapper;
import com.example.accessManager.repository.*;
import com.example.accessManager.service.AuditTrailService;
import com.example.accessManager.service.TeamService;
import com.example.accessManager.service.UserService;
import com.example.accessManager.wrapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    private final AccessRequestRepository accessRequestRepository;
    private final UserMapper userMapper;
    private final LoginRequestRepository loginRequestRepository;
    private final DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    @Override
    public List<TeamDTO> getAllTeams() {
        List<Team> teamList = teamRepository.findAll();
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
                            userService.updateAccessMode(userAccessModeDetailsWrapper,false);
                        } catch (NotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
                auditTrailService.addAuditEntry(ActionType.ACCESS_MODE_CHANGE,"Access Mode is changed to \"Override team access\" and all feature's access has been set to false because of team inactivation","",EntityType.USER,x.getId());
            });
            userRepository.saveAll(usersList);

        }

    }

    @Override
    public List<AuditDTO> getAuditLogs(Long id) throws NotFoundException {
        return auditTrailService.getAuditLogs(id,"Team");
    }

    @Override
    public List<TeamDTO> getTeamData(Long id) throws NotFoundException {
        Team team = teamRepository.findById(id).orElseThrow(() -> new NotFoundException("Team not found with id " + id));
        return List.of(teamMapper.teamToTeamDto(team));
    }

    @Override
    public List<AccessRequestDTO> getPendingRequests(UserDetails userDetails) throws NotFoundException {
        String userName = userDetails.getUsername();
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new NotFoundException("User not found with username : " + userName));
        List<AccessRequestDTO> accessRequestDTOS = new ArrayList<>();
        List<AccessRequest> accessRequests = new ArrayList<>();
        if(user.getPlatformRole().equals("PLATFORM_ADMIN")){
            accessRequests = accessRequestRepository.findAll();

        }else if(user.getPlatformRole().equals("TEAM_ADMIN")){
            List<User> users = userRepository.findAllByTeam_Id(user.getTeam().getId());
            List<Long> usersId = users.stream().map(User::getId).toList();
            accessRequests = accessRequestRepository.findAllByUser_IdIn(usersId);

        }
        accessRequests.forEach(x -> {
            if(x.getRequestStatus().equals(PendingRequestStatus.PENDING)) {
                User currUser;
                try {
                    currUser = userRepository.findById(x.getUser().getId()).orElseThrow(() -> new NotFoundException("user not found with id: " + x.getUser().getId()));
                } catch (NotFoundException e) {
                    throw new RuntimeException(e);
                }
                AccessRequestDTO accessRequestDTO = userMapper.accessRequestToAccessRequestDto(x);
                accessRequestDTO.setEmail(currUser.getEmail());
                accessRequestDTO.setTeamId(currUser.getTeam().getId());
                accessRequestDTO.setTeamName(currUser.getTeam().getName());
                accessRequestDTO.setAccessMode(currUser.getAccessMode().name());
                AccessControlDTO accessControlDTO;
                try {
                    accessControlDTO = userService.getUserPermissions(x.getUser().getId());
                } catch (NotFoundException e) {
                    throw new RuntimeException(e);
                }
                accessRequestDTO.setOtherFeatures(accessControlDTO);
                accessRequestDTOS.add(accessRequestDTO);
            }
        });
        return accessRequestDTOS;
    }

    @Override
    public void saveRequestDecision(AccessRequestDTO accessRequestDTO) throws NotFoundException {

        if(accessRequestDTO.getRequestDecision().equals("APPROVED")) {
            List<FeatureAccessWrapper> featureAccessWrappers = new ArrayList<>();
            if (accessRequestDTO.getAccessMode().equals("INHERIT_TEAM_ACCESS")) {
                List<TeamAccessControl> teamAccessControls = teamAccessControlRepository.findAllByTeam_Id(accessRequestDTO.getTeamId());
                List<Long> featureIds = featureRepository.findAll().stream().map(Feature::getId).toList();
                featureIds.forEach(x ->
                {

                    TeamAccessControl teamAccessControl;
                    try {
                        teamAccessControl = teamAccessControls.stream().filter(t -> (t.getTeam().getId().equals(accessRequestDTO.getTeamId())) && (t.getFeature().getId().equals(x))).findFirst().orElseThrow(() -> new NotFoundException("Team Access not found"));
                    } catch (NotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    featureAccessWrappers.add(FeatureAccessWrapper.builder()
                            .featureId(x)
                            .access(x.equals(accessRequestDTO.getFeatureId()) ? accessRequestDTO.getRequestType().equals("GRANT") : teamAccessControl.isHasAccess())
                            .build());
                });
                auditTrailService.addAuditEntry(ActionType.ACCESS_REQUEST_APPROVAL,"Access " + accessRequestDTO.getRequestType() + " request approved for feature " + accessRequestDTO.getFeatureName() + " and other inherited accesses will be transferred to override mode","",EntityType.ACCESS_REQUEST, accessRequestDTO.getId());
            } else {
                featureAccessWrappers.add(FeatureAccessWrapper.builder().featureId(accessRequestDTO.getFeatureId()).access(accessRequestDTO.getRequestType().equals("GRANT")).build());
                auditTrailService.addAuditEntry(ActionType.ACCESS_REQUEST_APPROVAL,"Access " + accessRequestDTO.getRequestType() +  "  request approved for feature " + accessRequestDTO.getFeatureName(),"",EntityType.ACCESS_REQUEST, accessRequestDTO.getId());
            }

            FeatureAccessDetailsWrapper featureAccessDetailsWrapper = new FeatureAccessDetailsWrapper();
            featureAccessDetailsWrapper.setFeatureAccessWrapperList(featureAccessWrappers);
            UserAccessModeDetailsWrapper userAccessModeDetailsWrapper = new UserAccessModeDetailsWrapper();
            userAccessModeDetailsWrapper.setUserId(accessRequestDTO.getUserId());
            userAccessModeDetailsWrapper.setAccessMode(AccessMode.OVERRIDE_TEAM_ACCESS.name());
            userAccessModeDetailsWrapper.setFeatureAccessDetailsWrapper(featureAccessDetailsWrapper);
            userService.updateAccessMode(userAccessModeDetailsWrapper, false);
        }
        accessRequestDTO.setRequestStatus(accessRequestDTO.getRequestDecision());
        userService.saveAccessRequest(accessRequestDTO);
        auditTrailService.addAuditEntry(ActionType.ACCESS_REQUEST_APPROVAL,"Access request rejected for feature " + accessRequestDTO.getFeatureName(), "",EntityType.ACCESS_REQUEST, accessRequestDTO.getId());
    }

    @Override
    public List<LoginRequestDTO> getAllLoginRequests() {
        List<LoginRequest> loginRequests = loginRequestRepository.findAllByIsActiveTrue();
        List<LoginRequestDTO> loginRequestDTOS = new ArrayList<>();
        loginRequests.forEach(x -> {
            LoginRequestDTO loginRequestDTO = LoginRequestDTO.builder()
                    .id(x.getId())
                    .name(x.getName())
                    .email(x.getEmail())
                    .empId(x.getEmpId())
                    .team(x.getTeam())
                    .role(x.getRole())
                    .createdDate(dateFormat.format(x.getCreatedDate()))
                    .build();
            loginRequestDTOS.add(loginRequestDTO);
        });
        return loginRequestDTOS;
    }
}
