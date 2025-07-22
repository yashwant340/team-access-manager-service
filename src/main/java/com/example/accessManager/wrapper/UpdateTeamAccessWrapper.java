package com.example.accessManager.wrapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTeamAccessWrapper {
    private Long id;
    private Long teamId;
    private Long featureId;
    private boolean hasAccess;
}
