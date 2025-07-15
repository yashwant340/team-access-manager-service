package com.example.accessManager.service;

import com.example.accessManager.dto.UserDTO;
import com.example.accessManager.wrapper.NewUserDetailsWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO addNewUser(NewUserDetailsWrapper wrapper);
}
