package com.hackerrank.sample.service;

import com.hackerrank.sample.dto.CreateModelRequest;
import com.hackerrank.sample.dto.ModelComparisonResponse;
import com.hackerrank.sample.dto.ModelResponseDto;
import java.util.List;

public interface ModelService {
    void deleteAllModels();
    void deleteModelById(Long id);

    void createModel(CreateModelRequest model);

    ModelResponseDto getModelById(Long id);

    List<ModelResponseDto> getAllModels();

    ModelComparisonResponse compareModels(List<Long> ids);

    List<ModelResponseDto> getModelsByName(String name);
}
