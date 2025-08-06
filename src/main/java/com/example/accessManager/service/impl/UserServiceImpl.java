package com.example.accessManager.service.impl;

import com.example.accessManager.dto.*;
import com.example.accessManager.entity.*;
import com.example.accessManager.enums.AccessMode;
import com.example.accessManager.enums.ActionType;
import com.example.accessManager.enums.EntityType;
import com.example.accessManager.exceptions.NotFoundException;
import com.example.accessManager.mapper.TeamMapper;
import com.example.accessManager.mapper.UserMapper;
import com.example.accessManager.repository.TeamAccessControlRepository;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuditTrailService auditTrailService;
    private final UserAccessControlRepository userAccessControlRepository;
    private final TeamAccessControlRepository teamAccessControlRepository;
    private final TeamMapper teamMapper;

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> userList = userRepository.findAllByIsActiveTrue();
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
    public void updateAccessMode(UserAccessModeDetailsWrapper wrapper) throws NotFoundException {
        User user = userRepository.findById(wrapper.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with ID : {}" + wrapper.getUserId()));

        if(user.getAccessMode().toString().equals(wrapper.getAccessMode())){
            auditTrailService.addAuditEntry(ActionType.ACCESS_MODE_CHANGE,"Access Mode changed to " + wrapper.getAccessMode(),"",EntityType.USER, wrapper.getUserId());
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
                auditTrailService.addAuditEntry(ActionType.USER_ACCESS_CHANGE,overrides.isHasAccess() ? "Access Provisioned" : "Access Revoked","",EntityType.USER_ACCESS, overrides.getId());
            }

        }else{
            user.setAccessMode(AccessMode.INHERIT_TEAM_ACCESS);
            List<UserAccessControl> userAccessControlList = userAccessControlRepository.findAllByUser_IdInAndIsActiveTrue(List.of(wrapper.getUserId()));
            if(!userAccessControlList.isEmpty()){
                for(UserAccessControl access: userAccessControlList){
                    access.setIsActive(Boolean.FALSE);
                    access.setUpdatedDate(new Date());
                    auditTrailService.addAuditEntry(ActionType.USER_ACCESS_CHANGE,"Access revoked","",EntityType.USER_ACCESS, access.getId());
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
}
