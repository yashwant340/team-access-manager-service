package com.example.accessManager.mapper;

import com.example.accessManager.dto.AccessRequestDTO;
import com.example.accessManager.dto.LoginUserDTO;
import com.example.accessManager.dto.UserAccessControlDTO;
import com.example.accessManager.dto.UserDTO;
import com.example.accessManager.entity.*;
import com.example.accessManager.enums.AccessMode;
import com.example.accessManager.enums.PendingRequestStatus;
import com.example.accessManager.wrapper.UserDetailsWrapper;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class UserMapper {
    private final DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    public User newUserDetailsWrapperToUser(UserDetailsWrapper wrapper){
        return User.builder()
                .email(wrapper.getEmail())
                .name(wrapper.getName())
                .role(wrapper.getRole())
                .empId(Long.valueOf(wrapper.getEmpId()))
                .team(Team.builder().id(wrapper.getTeamId()).build())
                .createdDate(new Date())
                .accessMode(wrapper.isInheritTeamAccess() ? AccessMode.INHERIT_TEAM_ACCESS : AccessMode.OVERRIDE_TEAM_ACCESS)
                .isActive(true)
                .build();
    }

    public UserDTO userToUserDto(User user){
        return UserDTO.builder()
                .id(user.getId())
                .empId(String.valueOf(user.getEmpId()))
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .teamId(user.getTeam().getId())
                .teamName(user.getTeam().getName())
                .accessMode(user.getAccessMode().toString())
                .isActive(user.getIsActive())
                .build();
    }

    public UserAccessControlDTO accessControlToDTO(UserAccessControl userAccessControl){
        return UserAccessControlDTO.builder()
                .userId(userAccessControl.getUser().getId())
                .id(userAccessControl.getId())
                .featureId(userAccessControl.getFeature().getId())
                .featureName(userAccessControl.getFeature().getName())
                .userName(userAccessControl.getUser().getName())
                .hasAccess(userAccessControl.isHasAccess())
                .build();
    }

    public LoginUserDTO userToLoginUserDto(User user) {
        return LoginUserDTO.builder()
                .username(user.getUsername())
                .platformRole(user.getPlatformRole())
                .email(user.getEmail())
                .name(user.getName())
                .id(user.getId())
                .teamId(user.getTeam().getId())
                .build();
    }

    public AccessRequest accessRequestDtoToAccessRequest(AccessRequestDTO accessRequestDTO){
        return AccessRequest.builder()
                .user(User.builder().id(accessRequestDTO.getUserId()).build())
                .feature(Feature.builder().id(accessRequestDTO.getFeatureId()).build())
                .requestedOn(new Date())
                .requestStatus(PendingRequestStatus.valueOf(accessRequestDTO.getRequestStatus()))
                .requestType(accessRequestDTO.getRequestType())
                .isActive(true)
                .updatedDate(new Date())
                .build();
    }

    public AccessRequestDTO accessRequestToAccessRequestDto(AccessRequest accessRequest){
        return AccessRequestDTO.builder()
                .id(accessRequest.getId())
                .userId(accessRequest.getUser().getId())
                .name(accessRequest.getUser().getName())
                .requestStatus(accessRequest.getRequestStatus().name())
                .requestType(accessRequest.getRequestType())
                .featureId(accessRequest.getFeature().getId())
                .featureName(accessRequest.getFeature().getName())
                .requestedOn(dateFormat.format(accessRequest.getRequestedOn()))
                .build();
    }
}
