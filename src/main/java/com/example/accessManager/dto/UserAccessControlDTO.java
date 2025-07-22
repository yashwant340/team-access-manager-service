package com.example.accessManager.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAccessControlDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long featureId;
    private String featureName;
    private boolean hasAccess;
}
