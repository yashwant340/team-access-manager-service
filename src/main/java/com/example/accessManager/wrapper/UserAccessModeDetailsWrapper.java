package com.example.accessManager.wrapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAccessModeDetailsWrapper {
    private Long userId;
    private String accessMode;
    private FeatureAccessDetailsWrapper featureAccessDetailsWrapper;

}
