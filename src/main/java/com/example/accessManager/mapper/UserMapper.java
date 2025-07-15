package com.example.accessManager.mapper;

import com.example.accessManager.dto.UserDTO;
import com.example.accessManager.entity.Team;
import com.example.accessManager.entity.User;
import com.example.accessManager.wrapper.NewUserDetailsWrapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User newUserDetailsWrapperToUser(NewUserDetailsWrapper wrapper){
        return User.builder()
                .email(wrapper.getEmail())
                .name(wrapper.getName())
                .role(wrapper.getRole())
                .team(Team.builder().id(wrapper.getTeamId()).build())
                .build();
    }

    public UserDTO userToUserDto(User user){
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .teamId(user.getTeam().getId())
                .isActive(user.getIsActive())
                .build();
    }
}
