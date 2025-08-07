package com.example.accessManager.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserDTO {

    private Long id;
    private String username;
    private String name;
    private String email;
    private String platformRole;
    private Long teamId;

}
