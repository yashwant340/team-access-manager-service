package com.example.accessManager.wrapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsWrapper {
    private Long id;
    private String name;
    private Long teamId;
    private String teamName;
    private String empId;
    private String email;
    private String role;
    private boolean active;
    private boolean inheritTeamAccess;
}
