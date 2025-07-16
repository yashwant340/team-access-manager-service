package com.example.accessManager.service;

import com.example.accessManager.dto.FeatureDTO;
import com.example.accessManager.wrapper.NewFeatureDetailsWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FeatureService {
    List<FeatureDTO> getAllFeatures();

    FeatureDTO addNewFeature(NewFeatureDetailsWrapper wrapper);
}
