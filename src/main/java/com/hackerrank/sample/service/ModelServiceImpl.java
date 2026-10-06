package com.hackerrank.sample.service;

import com.hackerrank.sample.dto.CreateModelRequest;
import com.hackerrank.sample.dto.ModelComparisonResponse;
import com.hackerrank.sample.dto.ModelResponseDto;
import com.hackerrank.sample.exception.BadResourceRequestException;
import com.hackerrank.sample.exception.NoSuchResourceFoundException;
import com.hackerrank.sample.model.Model;
import com.hackerrank.sample.repository.ModelRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("modelService")
public class ModelServiceImpl implements ModelService {
    private final ModelRepository modelRepository;
    private final ModelMapper modelMapper;

    public ModelServiceImpl(ModelRepository modelRepository, ModelMapper modelMapper) {
        this.modelRepository = modelRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public void deleteAllModels() {
        modelRepository.deleteAllInBatch();
    }

    @Override
    @Transactional
    public void deleteModelById(Long id) {
        if (!modelRepository.existsById(id)) {
            throw new NoSuchResourceFoundException("No resource found for the provided id.");
        }

        modelRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void createModel(CreateModelRequest modelRequest) {
        Model model = modelMapper.map(modelRequest, Model.class);
        modelRepository.save(model);
    }

    @Override
    @Transactional(readOnly = true)
    public ModelResponseDto getModelById(Long id) {
        Model model = modelRepository.findById(id)
                .orElseThrow(() -> new NoSuchResourceFoundException("No product with given id found."));

        return modelMapper.map(model, ModelResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModelResponseDto> getAllModels() {
        return modelRepository.findAll().stream()
                .map(model -> modelMapper.map(model, ModelResponseDto.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ModelComparisonResponse compareModels(List<Long> ids) {
        if (ids.isEmpty()) {
            throw new BadResourceRequestException("ids must not be empty");
        }

        List<Model> models = modelRepository.findAllByIdIn(ids);

        if (models.size() != ids.size()) {
            throw new NoSuchResourceFoundException("One or more informed ids were not found.");
        }

        IntSummaryStatistics ratingStatistics = models.stream()
                .mapToInt(Model::getRating)
                .summaryStatistics();
        BigDecimal lowestPrice = null;
        BigDecimal highestPrice = null;
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<ModelResponseDto> responseModels = new ArrayList<>(models.size());

        for (Model model : models) {
            BigDecimal price = model.getPrice();

            if (lowestPrice == null || price.compareTo(lowestPrice) < 0) {
                lowestPrice = price;
            }

            if (highestPrice == null || price.compareTo(highestPrice) > 0) {
                highestPrice = price;
            }

            totalPrice = totalPrice.add(price);
            responseModels.add(modelMapper.map(model, ModelResponseDto.class));
        }

        BigDecimal averagePrice = totalPrice.divide(BigDecimal.valueOf(models.size()), 2, RoundingMode.HALF_UP);

        ModelComparisonResponse response = new ModelComparisonResponse();
        response.setModels(responseModels);
        response.setLowestPrice(lowestPrice);
        response.setHighestPrice(highestPrice);
        response.setAveragePrice(averagePrice);
        response.setLowestRating(ratingStatistics.getMin());
        response.setHighestRating(ratingStatistics.getMax());
        response.setAverageRating(ratingStatistics.getAverage());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModelResponseDto> getModelsByName(String name) {
        return modelRepository.findByNameContainingIgnoreCase(name).stream()
                .map(model -> modelMapper.map(model, ModelResponseDto.class))
                .toList();
    }
}
