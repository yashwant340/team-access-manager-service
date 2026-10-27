package com.example.accessManager.mapper;

import com.example.accessManager.dto.TeamDTO;
import com.example.accessManager.dto.UserDTO;
import com.example.accessManager.entity.Team;
import com.example.accessManager.entity.User;
import com.example.accessManager.enums.AccessMode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeamMapperTest {

    private final TeamMapper teamMapper = new TeamMapper(new UserMapper());

    private Team teamWith(Boolean... activeFlags) {
        Team team = Team.builder().id(1L).name("Platform").isActive(true).users(new ArrayList<>()).build();
        for (int i = 0; i < activeFlags.length; i++) {
            team.getUsers().add(User.builder()
                    .id((long) i + 1)
                    .empId(1000L + i)
                    .name("User " + (i + 1))
                    .email("user" + (i + 1) + "@example.com")
                    .role("USER")
                    .team(team)
                    .accessMode(AccessMode.INHERIT_TEAM_ACCESS)
                    .isActive(activeFlags[i])
                    .build());
        }
        return team;
    }

    @Test
    void countsOnlyActiveUsersAsMembers() {
        TeamDTO dto = teamMapper.teamToTeamDto(teamWith(true, false, true));

        assertThat(dto.getUserList()).extracting(UserDTO::getId).containsExactly(1L, 3L);
    }

    @Test
    void teamWithOnlyInactiveUsersHasNoMembers() {
        assertThat(teamMapper.teamToTeamDto(teamWith(false, false)).getUserList()).isEmpty();
    }

    @Test
    void keepsAllUsersWhenEveryoneIsActive() {
        assertThat(teamMapper.teamToTeamDto(teamWith(true, true)).getUserList()).hasSize(2);
    }

    @Test
    void handlesATeamWithoutUsers() {
        Team team = Team.builder().id(2L).name("Empty").isActive(true).users(null).build();

        assertThat(teamMapper.teamToTeamDto(team).getUserList()).isEmpty();
    }

    @Test
    void keepsTheOtherTeamFields() {
        TeamDTO dto = teamMapper.teamToTeamDto(teamWith(true));

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Platform");
        assertThat(dto.isActive()).isTrue();
    }
}
