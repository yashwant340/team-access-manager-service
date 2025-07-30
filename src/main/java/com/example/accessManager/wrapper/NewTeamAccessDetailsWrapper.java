package com.example.accessManager.wrapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewTeamAccessDetailsWrapper {
    private Long featureId;
    private boolean hasAccess;
}
