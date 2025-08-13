package com.example.accessManager.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDashboardAccessDataDTO {
    private Long id;
    private Long userId;
    private Long featureId;
    private String featureName;
    private boolean hasAccess;
    private String lastUpdatedDate;
    private PendingRequestDTO pendingRequestDTO;

}
