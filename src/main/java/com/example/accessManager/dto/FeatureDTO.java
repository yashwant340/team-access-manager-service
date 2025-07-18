package com.example.accessManager.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeatureDTO {
    private Long id;
    private String name;
    private boolean isActive;
}
