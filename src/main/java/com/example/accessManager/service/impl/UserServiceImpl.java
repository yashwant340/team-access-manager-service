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
import com.example.accessManager.repository.AccessRequestRepository;
import com.example.accessManager.repository.UserAccessControlRepository;
import com.example.accessManager.repository.UserRepository;
import com.example.accessManager.service.AuditTrailService;
import com.example.accessManager.service.UserService;
import com.example.accessManager.wrapper.FeatureAccessWrapper;
import com.example.accessManager.wrapper.UserAccessModeDetailsWrapper;
import com.example.accessManager.wrapper.UserDetailsWrapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuditTrailService auditTrailService;
    private final UserAccessControlRepository userAccessControlRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final TeamMapper teamMapper;
    private final DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> userList = userRepository.findAll();
        List<UserDTO> userDTOList = new ArrayList<>();
        userList.forEach( x -> userDTOList.add(userMapper.userToUserDto(x)));
        return userDTOList;
    }

    @Override
    public AccessControlDTO getUserPermissions(Long id) throws NotFoundException {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with ID : {}" + id));
        AccessControlDTO accessControlDTO = new AccessControlDTO();
            List<TeamAccessControl> teamAccessControls = user.getTeam().getAccessControls();
            List<TeamAccessControlDTO> teamAccessControlDTOS = new ArrayList<>();
            for(TeamAccessControl accessControl: teamAccessControls){
                teamAccessControlDTOS.add(teamMapper.accessControlToDTO(accessControl));
            }
            accessControlDTO.setTeamAccessControlDTOS(teamAccessControlDTOS);
        if(!user.getAccessMode().equals(AccessMode.INHERIT_TEAM_ACCESS)){
            List<UserAccessControl> userAccessControls = userAccessControlRepository.findAllByUser_IdInAndIsActiveTrue(List.of(user.getId()));
            List<UserAccessControlDTO> userAccessControlDTOS = new ArrayList<>();
            for(UserAccessControl accessControl: userAccessControls){
                userAccessControlDTOS.add(userMapper.accessControlToDTO(accessControl));
            }
            accessControlDTO.setUserAccessControlDTOS(userAccessControlDTOS);
        }
        return accessControlDTO;
    }

    @Override
    public UserDTO addNewUser(UserDetailsWrapper wrapper){
        User addedUser = userRepository.save(userMapper.newUserDetailsWrapperToUser(wrapper));
        auditTrailService.addAuditEntry(ActionType.ADD_USER,"New User Added","",EntityType.USER,addedUser.getId());
        return userMapper.userToUserDto(addedUser);
    }

    @Override
    public UserDTO updateUser(UserDetailsWrapper wrapper){
        Optional<User> user = userRepository.findById(wrapper.getId());
        UserDTO userDTO = new UserDTO();
        if(user.isPresent()){
            User currUser = user.get();
            if(wrapper.getRole() != null && !wrapper.getRole().trim().equals(currUser.getRole().trim())) {
                auditTrailService.addAuditEntry(ActionType.UPDATE_USER,"User role updated from " +user.get().getRole() + " to " + wrapper.getRole(),"",EntityType.USER, wrapper.getId());
                currUser.setRole(wrapper.getRole());
            }
            if(wrapper.getEmail() != null && !wrapper.getEmail().equals(currUser.getEmail())) {
                auditTrailService.addAuditEntry(ActionType.UPDATE_USER,"User email updated from " +user.get().getEmail() + " to " + wrapper.getEmail(),"",EntityType.USER, wrapper.getId());
                currUser.setEmail(wrapper.getEmail());
            }
            if(wrapper.getTeamId() != null && !wrapper.getTeamId().equals(currUser.getTeam().getId())) {
                String name = currUser.getTeam().getName();
                currUser.setTeam(Team.builder().id(wrapper.getTeamId()).build());
                auditTrailService.addAuditEntry(ActionType.UPDATE_USER,"User team updated from " + name + " to " + wrapper.getTeamName(),"",EntityType.USER, wrapper.getId());
            }
            userDTO = userMapper.userToUserDto(userRepository.save(currUser));
        }

        return userDTO;
    }

    @Override
    public void updateAccessMode(UserAccessModeDetailsWrapper wrapper, boolean isAuditRequired) throws NotFoundException {
        User user = userRepository.findById(wrapper.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with ID : {}" + wrapper.getUserId()));

        if(!user.getAccessMode().toString().equals(wrapper.getAccessMode()) && isAuditRequired){
            if(wrapper.getAccessMode().equals("INHERIT_TEAM_ACCESS")){
                auditTrailService.addAuditEntry(ActionType.ACCESS_MODE_CHANGE, "Access Mode set to \"Inherit Team access\" and all existing access are revoked and inherited from teams access","",EntityType.USER, wrapper.getUserId());
            }else{
                auditTrailService.addAuditEntry(ActionType.ACCESS_MODE_CHANGE,"Access Mode changed to \"Override Team access\" " ,"",EntityType.USER, wrapper.getUserId());
            }
        }
        if(wrapper.getAccessMode().equals("OVERRIDE_TEAM_ACCESS")){
            user.setAccessMode(AccessMode.OVERRIDE_TEAM_ACCESS);
            List<UserAccessControl> userAccessControlList = new ArrayList<>();
            for(FeatureAccessWrapper overrides: wrapper.getFeatureAccessDetailsWrapper().getFeatureAccessWrapperList()){
                Long userId = wrapper.getUserId();
                Long featureId = overrides.getFeatureId();

                // Check if entry exists
                Optional<UserAccessControl> existing = userAccessControlRepository
                        .findByUser_IdAndFeature_Id(userId, featureId);

                if (existing.isPresent()) {
                    // Update existing entry
                    UserAccessControl control = existing.get();
                    control.setHasAccess(overrides.isAccess());
                    control.setUpdatedDate(new Date());
                    control.setIsActive(true);
                    userAccessControlList.add(control);
                } else {
                    // Insert new entry
                    UserAccessControl newControl = UserAccessControl.builder()
                            .user(User.builder().id(userId).build())
                            .feature(Feature.builder().id(featureId).build())
                            .hasAccess(overrides.isAccess())
                            .createdDate(new Date())
                            .updatedDate(new Date())
                            .isActive(true)
                            .build();
                    userAccessControlList.add(newControl);

                }
            }
            List<UserAccessControl> userAccessControl = userAccessControlRepository.saveAll(userAccessControlList);
            for(UserAccessControl overrides : userAccessControl){
                if(isAuditRequired){
                    auditTrailService.addAuditEntry(ActionType.USER_ACCESS_CHANGE,overrides.isHasAccess() ? "Access Provisioned" : "Access Revoked","",EntityType.USER_ACCESS, overrides.getId());
                }
            }

        }else{
            user.setAccessMode(AccessMode.INHERIT_TEAM_ACCESS);
            List<UserAccessControl> userAccessControlList = userAccessControlRepository.findAllByUser_IdInAndIsActiveTrue(List.of(wrapper.getUserId()));
            if(!userAccessControlList.isEmpty()){
                for(UserAccessControl access: userAccessControlList){
                    access.setIsActive(Boolean.FALSE);
                    access.setUpdatedDate(new Date());
                }
                userAccessControlRepository.saveAll(userAccessControlList);
            }
        }

        userRepository.save(user);
    }

    @Override
    public List<AuditDTO> getAuditLogs(Long id) throws NotFoundException {
        return auditTrailService.getAuditLogs(id, "User");
    }

    @Override
    public UserDTO deleteUser(Long id) throws NotFoundException {
        User user = userRepository.findById(id).orElseThrow( () -> new NotFoundException("User not found with " + id));
        user.setIsActive(false);
        UserDTO userDTO = userMapper.userToUserDto(userRepository.save(user));
        auditTrailService.addAuditEntry(ActionType.UPDATE_USER, "User Deleted", "",EntityType.USER,id);
        return userDTO;
    }

    @Override
    public List<UserDTO> getAllUsersOfTeam(Long id) {
        List<User> userList = userRepository.findAllByTeam_Id(id);
        List<UserDTO> userDTOList = new ArrayList<>();
        userList.forEach( x -> userDTOList.add(userMapper.userToUserDto(x)));
        return userDTOList;
    }

    @Override
    public List<UserDashboardAccessDataDTO> getAccessData(Long id) throws NotFoundException {
        List<UserDashboardAccessDataDTO> userDashboardAccessDataDTOS = new ArrayList<>();

        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with " + id));
        List<AccessRequest> accessRequests = accessRequestRepository.findAllByUser_IdAndIsActiveTrue(id);

        if (user.getAccessMode().equals(AccessMode.OVERRIDE_TEAM_ACCESS)) {
            List<UserAccessControl> userAccessControls = userAccessControlRepository.findAllByUser_IdInAndIsActiveTrue(Collections.singletonList(id));
            for (UserAccessControl userAccessControl : userAccessControls) {
                UserDashboardAccessDataDTO userDashboardAccessDataDTO = new UserDashboardAccessDataDTO();
                userDashboardAccessDataDTO.setId(userAccessControl.getId());
                userDashboardAccessDataDTO.setUserId(id);
                userDashboardAccessDataDTO.setFeatureId(userAccessControl.getFeature().getId());
                userDashboardAccessDataDTO.setFeatureName(userAccessControl.getFeature().getName());
                userDashboardAccessDataDTO.setHasAccess(userAccessControl.isHasAccess());
                userDashboardAccessDataDTO.setLastUpdatedDate(dateFormat.format(userAccessControl.getUpdatedDate()));
                for (AccessRequest accessRequest : accessRequests) {
                    if (Objects.equals(accessRequest.getUser().getId(), id) && Objects.equals(accessRequest.getFeature().getId(), userAccessControl.getFeature().getId())) {
                        PendingRequestDTO pendingRequestDTO = new PendingRequestDTO();
                        pendingRequestDTO.setId(accessRequest.getId());
                        pendingRequestDTO.setRequestedOn(dateFormat.format(accessRequest.getRequestedOn()));
                        pendingRequestDTO.setRequestStatus(accessRequest.getRequestStatus().name());
                        pendingRequestDTO.setRequestType(accessRequest.getRequestType());
                        userDashboardAccessDataDTO.setPendingRequestDTO(pendingRequestDTO);
                    }
                }
                userDashboardAccessDataDTOS.add(userDashboardAccessDataDTO);
            }
        }else{
            List<TeamAccessControl> teamAccessControls = user.getTeam().getAccessControls();
            for (TeamAccessControl teamAccessControl : teamAccessControls) {
                UserDashboardAccessDataDTO userDashboardAccessDataDTO = new UserDashboardAccessDataDTO();
                userDashboardAccessDataDTO.setId(teamAccessControl.getId());
                userDashboardAccessDataDTO.setUserId(id);
                userDashboardAccessDataDTO.setFeatureId(teamAccessControl.getFeature().getId());
                userDashboardAccessDataDTO.setFeatureName(teamAccessControl.getFeature().getName());
                userDashboardAccessDataDTO.setHasAccess(teamAccessControl.isHasAccess());
                userDashboardAccessDataDTO.setLastUpdatedDate(dateFormat.format(teamAccessControl.getUpdatedDate()));
                for (AccessRequest accessRequest : accessRequests) {
                    if (Objects.equals(accessRequest.getUser().getId(), id) && accessRequest.getFeature().getId() == teamAccessControl.getFeature().getId()) {
                        PendingRequestDTO pendingRequestDTO = new PendingRequestDTO();
                        pendingRequestDTO.setId(accessRequest.getId());
                        pendingRequestDTO.setRequestedOn(dateFormat.format(accessRequest.getRequestedOn()));
                        pendingRequestDTO.setRequestStatus(accessRequest.getRequestStatus().name());
                        pendingRequestDTO.setRequestType(accessRequest.getRequestType());
                        userDashboardAccessDataDTO.setPendingRequestDTO(pendingRequestDTO);
                    }
                }
                userDashboardAccessDataDTOS.add(userDashboardAccessDataDTO);
            }
        }
        return userDashboardAccessDataDTOS;

    }

    @Override
    public void saveAccessRequest(AccessRequestDTO accessRequestDTO) throws NotFoundException {
        UserDashboardAccessDataDTO userDashboardAccessDataDTO = new UserDashboardAccessDataDTO();
        if(accessRequestDTO.getId() != 0){
            AccessRequest accessRequest = accessRequestRepository.findByIdAndIsActiveTrue(accessRequestDTO.getId());
            accessRequest.setRequestStatus(PendingRequestStatus.valueOf(accessRequestDTO.getRequestStatus()));
            accessRequest.setUpdatedDate(new Date());
            accessRequest.setIsActive(false);
            accessRequestRepository.save(accessRequest);
            String action = getActionString(accessRequestDTO);
            auditTrailService.addAuditEntry(ActionType.ACCESS_REQUEST,action,"",EntityType.ACCESS_REQUEST, accessRequest.getId());
        }else{
            AccessRequest accessRequest = accessRequestRepository.save(userMapper.accessRequestDtoToAccessRequest(accessRequestDTO));
            auditTrailService.addAuditEntry(ActionType.ACCESS_REQUEST,getActionString(accessRequestDTO),"",EntityType.ACCESS_REQUEST, accessRequest.getId());
        }

    }

    @Override
    public UserDTO getUser(Long id) throws NotFoundException {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id : " + id));
        return userMapper.userToUserDto(user);
    }

    @Override
    public List<AuditDTO> getUserDashboardAudit(Long id) throws NotFoundException {
        return auditTrailService.getAuditLogs(id,"UserDashBoard");
    }

    private static String getActionString(AccessRequestDTO accessRequest) {
        String action;
        String type = accessRequest.getRequestType().equals("GRANT") ? " grant " : " revoke ";
        action = switch (PendingRequestStatus.valueOf(accessRequest.getRequestStatus())) {
            case PENDING -> "Access" + type + "request raised for " + accessRequest.getFeatureName();
            case APPROVED -> "Access" + type + "request approved for " + accessRequest.getFeatureName();
            case REJECTED -> "Access" + type + "request rejected for " + accessRequest.getFeatureName();
            case CANCELLED -> "Access" + type + "request cancelled by user for " + accessRequest.getFeatureName();
        };
        return action;
    }
}
