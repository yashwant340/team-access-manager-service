package com.example.accessManager.controller;

import com.example.accessManager.dto.TeamDTO;
import com.example.accessManager.service.TeamService;
import com.example.accessManager.wrapper.NewTeamDetailsWrapper;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/addNew")
    public TeamDTO addNewTeam(@RequestBody NewTeamDetailsWrapper wrapper){
        return teamService.addNewteam(wrapper);
    }
}
