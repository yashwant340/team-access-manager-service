package com.example.accessManager.service.impl;

import com.example.accessManager.dto.FeatureDTO;
import com.example.accessManager.entity.Feature;
import com.example.accessManager.mapper.FeatureMapper;
import com.example.accessManager.repository.FeatureRepository;
import com.example.accessManager.service.FeatureService;
import com.example.accessManager.wrapper.NewFeatureDetailsWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FeatureServiceImpl implements FeatureService {

    private final FeatureRepository featureRepository;
    private final FeatureMapper featureMapper;

    @Override
    public List<FeatureDTO> getAllFeatures() {
        List<Feature> featureList = featureRepository.findAllByIsActiveTrue();
        List<FeatureDTO> featureDTOList = new ArrayList<>();
        featureList.forEach(x -> featureDTOList.add(featureMapper.featureToFeatureDto(x)));
        return featureDTOList;
    }

    @Override
    public FeatureDTO addNewFeature(NewFeatureDetailsWrapper wrapper) {
        Feature feature = featureRepository.save(featureMapper.newTFeatureDetailsWrapperToFeature(wrapper));
        return featureMapper.featureToFeatureDto(feature);
    }
}
