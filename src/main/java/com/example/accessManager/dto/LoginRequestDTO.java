package com.example.accessManager.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {
    private Long id;
    private String name;
    private String email;
    private String empId;
    private String team;
    private String role;
    private String createdDate;
    private String updatedDate;

}
