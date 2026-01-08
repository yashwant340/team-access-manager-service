package com.example.accessManager.controller;

import com.example.accessManager.dto.LoginRequestDTO;
import com.example.accessManager.service.AdminService;
import com.example.accessManager.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/team-access-manager/admin")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/login-request/pending")
    public List<LoginRequestDTO> getLoginRequests(){
        return adminService.getAllLoginRequests();
    }

    @PostMapping("/login-request/approve")
    public void approveLoginReq(@RequestParam("reqId") Long id,@RequestParam("teamId") Long teamId){
         adminService.approveRequest(id, teamId);
    }
}
