package com.example.accessManager.service.impl;

import com.example.accessManager.dto.AccessControlDTO;
import com.example.accessManager.dto.TeamAccessControlDTO;
import com.example.accessManager.dto.UserAccessControlDTO;
import com.example.accessManager.dto.UserDTO;
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
        if(user.getAccessMode().equals(AccessMode.INHERIT_TEAM_ACCESS)){
            List<TeamAccessControl> teamAccessControls = user.getTeam().getAccessControls();
            List<TeamAccessControlDTO> teamAccessControlDTOS = new ArrayList<>();
            for(TeamAccessControl accessControl: teamAccessControls){
                teamAccessControlDTOS.add(teamMapper.accessControlToDTO(accessControl));
            }
            accessControlDTO.setTeamAccessControlDTOS(teamAccessControlDTOS);
        }else{
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
            currUser.setRole(wrapper.getRole());
            currUser.setEmail(wrapper.getEmail());
            currUser.setTeam(Team.builder().id(wrapper.getTeamId()).build());
            currUser.setIsActive(wrapper.isActive());
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
                UserAccessControl userAccessControl = UserAccessControl.builder()
                        .user(User.builder().id(wrapper.getUserId()).build())
                        .feature(Feature.builder().id(overrides.getFeatureId()).build())
                        .hasAccess(overrides.isAccess())
                        .createdDate(new Date())
                        .isActive(true)
                        .build();

                userAccessControlList.add(userAccessControl);
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
                    auditTrailService.addAuditEntry(ActionType.USER_ACCESS_CHANGE,"Access revoked","",EntityType.USER_ACCESS, access.getId());
                }

                userAccessControlRepository.saveAll(userAccessControlList);
            }
        }

        userRepository.save(user);
    }
}
