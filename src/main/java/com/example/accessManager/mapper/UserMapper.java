package com.example.accessManager.mapper;

import com.example.accessManager.dto.UserAccessControlDTO;
import com.example.accessManager.dto.UserDTO;
import com.example.accessManager.entity.Team;
import com.example.accessManager.entity.User;
import com.example.accessManager.entity.UserAccessControl;
import com.example.accessManager.enums.AccessMode;
import com.example.accessManager.wrapper.UserDetailsWrapper;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class UserMapper {
    public User newUserDetailsWrapperToUser(UserDetailsWrapper wrapper){
        return User.builder()
                .email(wrapper.getEmail())
                .name(wrapper.getName())
                .role(wrapper.getRole())
                .team(Team.builder().id(wrapper.getTeamId()).build())
                .createdDate(new Date())
                .accessMode(wrapper.isInheritTeamAccess() ? AccessMode.INHERIT_TEAM_ACCESS : AccessMode.OVERRIDE_TEAM_ACCESS)
                .isActive(true)
                .build();
    }

    public UserDTO userToUserDto(User user){
        return UserDTO.builder()
                .id(user.getId())
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
}
