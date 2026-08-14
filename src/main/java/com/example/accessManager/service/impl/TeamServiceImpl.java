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
import com.example.accessManager.utils.SecurityUtility;
import com.example.accessManager.wrapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final SecurityUtility securityUtility;

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
        List<UpdateTeamAccessWrapper> updateTeamAccessWrapperList = new ArrayList<>();
        wrapper.getAccessList().forEach(x -> updateTeamAccessWrapperList.add(UpdateTeamAccessWrapper.builder().id(0L).teamId(team.getId()).featureId(x.getFeatureId()).hasAccess(x.isHasAccess()).build()));
        List<TeamAccessControl> initialAccessControls = updateTeamAccessWrapperList.stream()
                .map(teamMapper::AccessWrappertoAccessControl)
                .toList();
        teamAccessControlRepository.saveAll(initialAccessControls);
        auditTrailService.addAuditEntry(
                ActionType.ADD_TEAM,
                "Team created with " + initialAccessControls.size() + " feature permissions configured",
                securityUtility.getCurrentUsername(),
                EntityType.TEAM,
                team.getId()
        );
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
                    if (existingTeamAccess.isHasAccess() != accessWrapper.isHasAccess()) {
                        existingTeamAccess.setHasAccess(accessWrapper.isHasAccess());
                        existingTeamAccess.setUpdatedDate(new Date());
                        teamAccessControl = teamAccessControlRepository.save(existingTeamAccess);
                        auditTrailService.addAuditEntry(ActionType.TEAM_ACCESS_CHANGE, teamAccessControl.isHasAccess() ? "Access granted" : "Access revoked", securityUtility.getCurrentUsername(),EntityType.TEAM_ACCESS, teamAccessControl.getId() );
                    } else {
                        teamAccessControl = existingTeamAccess;
                    }
                }

            } else {
                teamAccessControl= teamAccessControlRepository.save(teamMapper.AccessWrappertoAccessControl(accessWrapper));
                auditTrailService.addAuditEntry(ActionType.TEAM_ACCESS_CHANGE, teamAccessControl.isHasAccess() ? "Access configured as granted" : "Access configured as not granted",securityUtility.getCurrentUsername(),EntityType.TEAM_ACCESS, teamAccessControl.getId());
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
        auditTrailService.addAuditEntry(ActionType.UPDATE_TEAM,"Team Inactivated",securityUtility.getCurrentUsername(),EntityType.TEAM,id);
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

                    auditTrailService.addAuditEntry(ActionType.ACCESS_MODE_CHANGE,"Access mode changed to \"Override team access\" with all permissions set to not granted because the team was deactivated",securityUtility.getCurrentUsername(),EntityType.USER,x.getId());
                }
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
            if(Boolean.TRUE.equals(x.getIsActive()) && PendingRequestStatus.PENDING.equals(x.getRequestStatus())) {
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
                accessRequestDTO.setCurrentFeatureHasAccess(getCurrentFeatureAccess(currUser, accessRequestDTO));
                accessRequestDTOS.add(accessRequestDTO);
            }
        });
        return accessRequestDTOS;
    }

    @Override
    @Transactional
    public void saveRequestDecision(AccessRequestDTO accessRequestDTO, UserDetails reviewerDetails) throws NotFoundException {
        if (accessRequestDTO.getId() == null) {
            throw new IllegalArgumentException("A request ID is required");
        }

        PendingRequestStatus decision;
        try {
            decision = PendingRequestStatus.valueOf(accessRequestDTO.getRequestDecision());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        }
        if (decision != PendingRequestStatus.APPROVED && decision != PendingRequestStatus.REJECTED) {
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        }

        AccessRequest accessRequest = accessRequestRepository.findByIdAndIsActiveTrue(accessRequestDTO.getId());
        if (accessRequest == null || accessRequest.getRequestStatus() != PendingRequestStatus.PENDING) {
            throw new NotFoundException("Pending access request not found with ID: " + accessRequestDTO.getId());
        }

        User reviewer = userRepository.findByUsername(reviewerDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("Reviewer not found with username: " + reviewerDetails.getUsername()));
        User requestingUser = userRepository.findById(accessRequest.getUser().getId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + accessRequest.getUser().getId()));
        if (!canReviewRequest(reviewer, requestingUser)) {
            throw new AccessDeniedException("You are not permitted to review this request");
        }

        boolean approved = decision == PendingRequestStatus.APPROVED;
        boolean inheritedAccess = requestingUser.getAccessMode() == AccessMode.INHERIT_TEAM_ACCESS;
        String requestType = accessRequest.getRequestType();
        if (!"GRANT".equals(requestType) && !"REVOKE".equals(requestType)) {
            throw new IllegalArgumentException("Access request has an invalid request type");
        }
        Long featureId = accessRequest.getFeature().getId();
        String featureName = accessRequest.getFeature().getName();

        if(approved) {
            List<FeatureAccessWrapper> featureAccessWrappers = new ArrayList<>();
            if (inheritedAccess) {
                List<TeamAccessControl> teamAccessControls = teamAccessControlRepository.findAllByTeam_Id(requestingUser.getTeam().getId());
                List<Long> featureIds = featureRepository.findAll().stream().map(Feature::getId).toList();
                featureIds.forEach(x ->
                {

                    TeamAccessControl teamAccessControl;
                    try {
                        teamAccessControl = teamAccessControls.stream().filter(t -> (t.getTeam().getId().equals(requestingUser.getTeam().getId())) && (t.getFeature().getId().equals(x))).findFirst().orElseThrow(() -> new NotFoundException("Team Access not found"));
                    } catch (NotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    featureAccessWrappers.add(FeatureAccessWrapper.builder()
                            .featureId(x)
                            .access(x.equals(featureId) ? requestType.equals("GRANT") : teamAccessControl.isHasAccess())
                            .build());
                });
            } else {
                featureAccessWrappers.add(FeatureAccessWrapper.builder().featureId(featureId).access(requestType.equals("GRANT")).build());
            }

            FeatureAccessDetailsWrapper featureAccessDetailsWrapper = new FeatureAccessDetailsWrapper();
            featureAccessDetailsWrapper.setFeatureAccessWrapperList(featureAccessWrappers);
            UserAccessModeDetailsWrapper userAccessModeDetailsWrapper = new UserAccessModeDetailsWrapper();
            userAccessModeDetailsWrapper.setUserId(requestingUser.getId());
            userAccessModeDetailsWrapper.setAccessMode(AccessMode.OVERRIDE_TEAM_ACCESS.name());
            userAccessModeDetailsWrapper.setFeatureAccessDetailsWrapper(featureAccessDetailsWrapper);
            userService.updateAccessMode(userAccessModeDetailsWrapper, false);
        }
        accessRequestDTO.setRequestStatus(decision.name());
        userService.saveAccessRequest(accessRequestDTO);
        String action = approved
                ? "Access " + requestType.toLowerCase() + " request approved for " + featureName
                + (inheritedAccess ? "; inherited permissions were copied to a custom override" : "")
                : "Access " + requestType.toLowerCase() + " request rejected for " + featureName;
        auditTrailService.addAuditEntry(ActionType.ACCESS_REQUEST_APPROVAL, action, securityUtility.getCurrentUsername(), EntityType.ACCESS_REQUEST, accessRequestDTO.getId());
    }

    private Boolean getCurrentFeatureAccess(User user, AccessRequestDTO accessRequestDTO) {
        if (user.getAccessMode() == AccessMode.INHERIT_TEAM_ACCESS) {
            return teamAccessControlRepository.findAllByTeam_Id(user.getTeam().getId()).stream()
                    .filter(access -> access.getFeature().getId().equals(accessRequestDTO.getFeatureId()))
                    .map(TeamAccessControl::isHasAccess)
                    .findFirst()
                    .orElse(null);
        }

        return userAccessControlRepository.findByUser_IdAndFeature_Id(user.getId(), accessRequestDTO.getFeatureId())
                .filter(access -> Boolean.TRUE.equals(access.getIsActive()))
                .map(UserAccessControl::isHasAccess)
                .orElse(null);
    }

    private boolean canReviewRequest(User reviewer, User requestingUser) {
        if ("PLATFORM_ADMIN".equals(reviewer.getPlatformRole())) {
            return true;
        }

        return "TEAM_ADMIN".equals(reviewer.getPlatformRole())
                && reviewer.getTeam() != null
                && requestingUser.getTeam() != null
                && reviewer.getTeam().getId().equals(requestingUser.getTeam().getId());
    }


}
