package com.example.accessManager.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamAccessControlDTO {
    private Long id;
    private Long teamId;
    private String teamName;
    private Long featureId;
    private String featureName;
    private boolean hasAccess;
}
