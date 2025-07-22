package com.example.accessManager.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessControlDTO {
    List<TeamAccessControlDTO> teamAccessControlDTOS;
    List<UserAccessControlDTO> userAccessControlDTOS;

}
