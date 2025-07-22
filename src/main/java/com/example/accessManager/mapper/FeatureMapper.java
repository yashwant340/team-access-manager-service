package com.example.accessManager.mapper;

import com.example.accessManager.dto.FeatureDTO;
import com.example.accessManager.entity.Feature;
import com.example.accessManager.wrapper.NewFeatureDetailsWrapper;
import org.springframework.stereotype.Component;

@Component
public class FeatureMapper {
    public Feature newTFeatureDetailsWrapperToFeature(NewFeatureDetailsWrapper wrapper){
        return Feature.builder()
                .name(wrapper.getName())
                .isActive(true)
                .build();
    }

    public FeatureDTO featureToFeatureDto(Feature feature){
        return FeatureDTO.builder()
                .id(feature.getId())
                .name(feature.getName())
                .isActive(feature.getIsActive())
                .build();
    }
}
