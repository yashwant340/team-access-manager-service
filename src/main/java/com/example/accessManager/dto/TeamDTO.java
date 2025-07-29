package com.example.accessManager.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamDTO {
    private Long id;
    private String name;
    private boolean isActive;
    private List<UserDTO> userList;
}
