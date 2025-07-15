package com.example.accessManager.service.impl;

import com.example.accessManager.dto.UserDTO;
import com.example.accessManager.entity.User;
import com.example.accessManager.mapper.UserMapper;
import com.example.accessManager.repository.UserRepository;
import com.example.accessManager.service.UserService;
import com.example.accessManager.wrapper.NewUserDetailsWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> userList = userRepository.findAllByIsActiveTrue();
        List<UserDTO> userDTOList = new ArrayList<>();
        userList.forEach( x -> userDTOList.add(userMapper.userToUserDto(x)));
        return userDTOList;
    }

    @Override
    public UserDTO addNewUser(NewUserDetailsWrapper wrapper){
        User addedUser = userRepository.save(userMapper.newUserDetailsWrapperToUser(wrapper));
        return userMapper.userToUserDto(addedUser);
    }
}
