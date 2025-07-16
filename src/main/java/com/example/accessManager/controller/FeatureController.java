package com.example.accessManager.controller;

import com.example.accessManager.dto.FeatureDTO;
import com.example.accessManager.service.FeatureService;
import com.example.accessManager.wrapper.NewFeatureDetailsWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/team-access-manager/feature")
public class FeatureController {

    private final FeatureService featureService;

    @GetMapping("/getAll")
    public List<FeatureDTO> getAllFeatures(){
        return featureService.getAllFeatures();
    }

    @PostMapping("/addNew")
    public FeatureDTO addNewFeature(@RequestBody NewFeatureDetailsWrapper wrapper){
        return featureService.addNewFeature(wrapper);
    }
}
