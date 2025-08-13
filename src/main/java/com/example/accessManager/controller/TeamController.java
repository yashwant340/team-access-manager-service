package com.example.accessManager.controller;

import com.example.accessManager.dto.*;
import com.example.accessManager.exceptions.NotFoundException;
import com.example.accessManager.service.TeamService;
import com.example.accessManager.wrapper.NewTeamDetailsWrapper;
import com.example.accessManager.wrapper.UpdateTeamAccessWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/team-access-manager/team")
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/getAll")
    public List<TeamDTO> getAllTeams(){
        return teamService.getAllTeams();
    }

    @GetMapping("/")
    public List<TeamDTO> getTeamData(@RequestParam("teamId") Long id) throws NotFoundException {
        return teamService.getTeamData(id);
    }

    @PostMapping("/addNew")
    public TeamDTO addNewTeam(@RequestBody NewTeamDetailsWrapper wrapper){
        return teamService.addNewteam(wrapper);
    }

    @GetMapping("/auditLog")
    public List<AuditDTO> getAuditLogs(@RequestParam("teamId") Long id) throws NotFoundException {
        return teamService.getAuditLogs(id);
    }

    @PostMapping("/delete")
    public void deleteTeam(@RequestParam("teamId") Long id) throws NotFoundException {
        teamService.deleteTeam(id);
    }

    @PostMapping("/updateAccess")
    public List<TeamAccessControlDTO> provideAccess(@RequestBody List<UpdateTeamAccessWrapper> wrapper){
        return teamService.updateTeamAccess(wrapper);
    }

    @GetMapping("/team-permissions")
    public AccessControlDTO getTeamPermissions(@RequestParam("teamId") Long id) throws NotFoundException {
        return teamService.getTeamPermissions(id);
    }

    @GetMapping("/pending-request")
    public List<AccessRequestDTO> getPendingRequest(@AuthenticationPrincipal UserDetails userDetails) throws NotFoundException {
        return teamService.getPendingRequests(userDetails);
    }

    @GetMapping("/pending-login-request")
    public List<LoginRequestDTO> getLoginRequests(){
        return teamService.getAllLoginRequests();
    }

    @PostMapping("/request-decision")
    public void saveRequestDecision(@RequestBody AccessRequestDTO accessRequestDTO) throws NotFoundException {
        teamService.saveRequestDecision(accessRequestDTO);
    }
}
