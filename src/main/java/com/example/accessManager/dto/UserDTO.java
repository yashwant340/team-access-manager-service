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
    private String empId;
    private String email;
    private String role;
    private Long teamId;
    private String teamName;
    private String accessMode;
    private boolean isActive;
}
