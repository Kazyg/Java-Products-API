package com.hackerrank.sample.controller;

import com.hackerrank.sample.dto.CreateModelRequest;
import com.hackerrank.sample.dto.ModelComparisonRequest;
import com.hackerrank.sample.dto.ModelComparisonResponse;
import com.hackerrank.sample.dto.ModelResponseDto;
import com.hackerrank.sample.service.ModelService;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/products")
public class ModelController {
    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void createNewModel(@RequestBody @Valid CreateModelRequest model) {
        modelService.createModel(model);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllModels() {
        modelService.deleteAllModels();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModelById(@PathVariable Long id) {
        modelService.deleteModelById(id);
    }

    @GetMapping
    public List<ModelResponseDto> getAllModels() {
        return modelService.getAllModels();
    }

    @GetMapping("/{id}")
    public ModelResponseDto getModelById(@PathVariable Long id) {
        return modelService.getModelById(id);
    }

    @PostMapping(value = "/compare", consumes = "application/json")
    public ModelComparisonResponse compareModels(@RequestBody @Valid ModelComparisonRequest request) {
        return modelService.compareModels(request.getIds());
    }

    @GetMapping("/search")
    public List<ModelResponseDto> getModelsByName(@RequestParam String name) {
        return modelService.getModelsByName(name);
    }
}
