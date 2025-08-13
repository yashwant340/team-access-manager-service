package com.example.accessManager.controller;

import com.example.accessManager.dto.*;
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

    @GetMapping("/getUser")
    public UserDTO getUser(@RequestParam("userId") Long id) throws NotFoundException {
        return userService.getUser(id);
    }

    @GetMapping("/teamId/")
    public List<UserDTO> getUsersOfTeam(@RequestParam("teamId") Long id){
        return userService.getAllUsersOfTeam(id);
    }

    @PostMapping("/addNew")
    public UserDTO addNewUser(@RequestBody UserDetailsWrapper wrapper){
        return userService.addNewUser(wrapper);
    }

    @PostMapping("/updateUser")
    public UserDTO updateUser(@RequestBody UserDetailsWrapper wrapper){
        return userService.updateUser(wrapper);
    }

    @PostMapping("/deleteUser")
    public UserDTO deleteUser(@RequestParam("userId") Long id) throws NotFoundException {
        return userService.deleteUser(id);
    }

    @PostMapping("/updateAccessMode")
    public void updateAccessMode(@RequestBody UserAccessModeDetailsWrapper wrapper) throws NotFoundException {
        userService.updateAccessMode(wrapper, true);
    }

    @GetMapping("/user-permissions")
    public AccessControlDTO getUserPermissions(@RequestParam("userId") Long id) throws NotFoundException {
        return userService.getUserPermissions(id);
    }

    @GetMapping("/userAuditLog")
    public List<AuditDTO> getAuditLogs(@RequestParam("userId") Long id) throws NotFoundException {
        return userService.getAuditLogs(id);
    }

    @GetMapping("/userDashboard/accessData")
    public List<UserDashboardAccessDataDTO> getAccessData(@RequestParam("userId") Long id) throws NotFoundException {
        return userService.getAccessData(id);

    }

    @GetMapping("/userDashboard/auditLog")
    public List<AuditDTO> getUserAudit(@RequestParam("userId") Long id) throws NotFoundException {
        return userService.getUserDashboardAudit(id);
    }

    @PostMapping("/access-request")
    public void saveAccessRequest(@RequestBody AccessRequestDTO accessRequestDTO) throws NotFoundException {
        userService.saveAccessRequest(accessRequestDTO);
    }

}
