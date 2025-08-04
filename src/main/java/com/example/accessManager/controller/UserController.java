package com.example.accessManager.controller;

import com.example.accessManager.dto.AccessControlDTO;
import com.example.accessManager.dto.AuditDTO;
import com.example.accessManager.dto.UserDTO;
import com.example.accessManager.exceptions.NotFoundException;
import com.example.accessManager.service.UserService;
import com.example.accessManager.wrapper.UserAccessModeDetailsWrapper;
import com.example.accessManager.wrapper.UserDetailsWrapper;
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
    public UserDTO addNewUser(@RequestBody UserDetailsWrapper wrapper){
        return userService.addNewUser(wrapper);
    }

    @PostMapping("/updateUser")
    public UserDTO updateUser(@RequestBody UserDetailsWrapper wrapper){
        return userService.updateUser(wrapper);
    }

    @PostMapping("/updateAccessMode")
    public void updateAccessMode(@RequestBody UserAccessModeDetailsWrapper wrapper) throws NotFoundException {
        userService.updateAccessMode(wrapper);
    }

    @GetMapping("/user-permissions")
    public AccessControlDTO getUserPermissions(@RequestParam("userId") Long id) throws NotFoundException {
        return userService.getUserPermissions(id);
    }

    @GetMapping("/userAuditLog")
    public List<AuditDTO> getAuditLogs(@RequestParam("userId") Long id) throws NotFoundException {
        return userService.getAuditLogs(id);
    }
}
