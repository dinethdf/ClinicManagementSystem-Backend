package com.dinethdf.ClinicManagementSystem.DTO.Clinic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicCompleteContextDTO implements Serializable {

    private ClinicResponseDTO clinicInfo;

    private List<ClinicFeatureDTO> features;


    private Map<String, Boolean> permissionMap;

    public boolean hasFeature(String featureCode) {
        return permissionMap != null && permissionMap.getOrDefault(featureCode, false);
    }
}