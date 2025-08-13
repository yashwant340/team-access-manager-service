package com.example.accessManager.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessRequestDTO {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private Long teamId;
    private String teamName;
    private Long featureId;
    private String featureName;
    private String accessMode;
    private String requestType;
    private String requestStatus;
    private String requestedOn;
    private AccessControlDTO otherFeatures;
    private String requestDecision;
}
