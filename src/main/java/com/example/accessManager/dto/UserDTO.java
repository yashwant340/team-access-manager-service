package com.example.accessManager.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String team;
    private Long teamId;
    private Boolean isActive;
}
