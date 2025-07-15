package com.example.accessManager.controller;

import com.example.accessManager.dto.UserDTO;
import com.example.accessManager.service.UserService;
import com.example.accessManager.wrapper.NewUserDetailsWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/team-access-manager/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/getAll")
    public List<UserDTO> getAllUsers(){
        return userService.getAllUsers();
    }

    @PostMapping("/addNew")
    public UserDTO addNewUser(@RequestBody NewUserDetailsWrapper wrapper){
        return userService.addNewUser(wrapper);
    }
}
