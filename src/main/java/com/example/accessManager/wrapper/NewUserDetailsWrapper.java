package com.example.accessManager.wrapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewUserDetailsWrapper {
    private String name;
    private Long teamId;
    private String email;
    private String role;
}
