package com.hackerrank.sample.service;

import com.hackerrank.sample.dto.CreateModelRequest;
import com.hackerrank.sample.dto.ModelComparisonResponse;
import com.hackerrank.sample.dto.ModelResponseDto;
import com.hackerrank.sample.exception.BadResourceRequestException;
import com.hackerrank.sample.exception.NoSuchResourceFoundException;
import com.hackerrank.sample.model.Model;
import com.hackerrank.sample.repository.ModelRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelServiceImplTest {
    @Mock
    private ModelRepository modelRepository;

    @Mock
    private ModelMapper modelMapper;

    private ModelServiceImpl modelService;

    @BeforeEach
    void setUp() {
        modelService = new ModelServiceImpl(modelRepository, modelMapper);
    }

    @Test
    void shouldCreateModelAndSaveMappedModel() {
        CreateModelRequest request = createRequest("iPhone 15", 5299L, 5, "128GB, 6GB RAM");
        Model mappedModel = createModel(null, "iPhone 15", 5299L, 5);

        when(modelMapper.map(request, Model.class)).thenReturn(mappedModel);

        modelService.createModel(request);

        verify(modelMapper).map(request, Model.class);
        verify(modelRepository).save(mappedModel);
    }

    @Test
    void shouldReturnModelById() {
        Model model = createModel(1L, "iPhone 15", 5299L, 5);
        ModelResponseDto responseDto = createResponseDto(model);

        when(modelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(modelMapper.map(model, ModelResponseDto.class)).thenReturn(responseDto);

        ModelResponseDto response = modelService.getModelById(1L);

        assertThat(response).isSameAs(responseDto);
        verify(modelRepository).findById(1L);
        verify(modelMapper).map(model, ModelResponseDto.class);
    }

    @Test
    void shouldThrowWhenModelByIdDoesNotExist() {
        when(modelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> modelService.getModelById(999L))
                .isInstanceOf(NoSuchResourceFoundException.class)
                .hasMessage("No product with given id found.");

        verify(modelRepository).findById(999L);
    }

    @Test
    void shouldDeleteModelById() {
        when(modelRepository.existsById(1L)).thenReturn(true);

        modelService.deleteModelById(1L);

        verify(modelRepository).existsById(1L);
        verify(modelRepository).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeletingUnknownId() {
        when(modelRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> modelService.deleteModelById(999L))
                .isInstanceOf(NoSuchResourceFoundException.class)
                .hasMessage("No resource found for the provided id.");

        verify(modelRepository).existsById(999L);
    }

    @Test
    void shouldDeleteAllModels() {
        modelService.deleteAllModels();

        verify(modelRepository).deleteAllInBatch();
    }

    @Test
    void shouldReturnAllModels() {
        Model iphone15 = createModel(1L, "iPhone 15", 5299L, 5);
        Model galaxyS24 = createModel(2L, "Galaxy S24", 4699L, 4);
        ModelResponseDto iphone15Response = createResponseDto(iphone15);
        ModelResponseDto galaxyS24Response = createResponseDto(galaxyS24);

        when(modelRepository.findAll()).thenReturn(List.of(iphone15, galaxyS24));
        when(modelMapper.map(iphone15, ModelResponseDto.class)).thenReturn(iphone15Response);
        when(modelMapper.map(galaxyS24, ModelResponseDto.class)).thenReturn(galaxyS24Response);

        List<ModelResponseDto> response = modelService.getAllModels();

        assertThat(response).containsExactly(iphone15Response, galaxyS24Response);
        verify(modelRepository).findAll();
    }

    @Test
    void shouldReturnModelsByName() {
        Model redmiNote13 = createModel(3L, "Redmi Note 13", 1899L, 4);
        ModelResponseDto responseDto = createResponseDto(redmiNote13);

        when(modelRepository.findByNameContainingIgnoreCase("note")).thenReturn(List.of(redmiNote13));
        when(modelMapper.map(redmiNote13, ModelResponseDto.class)).thenReturn(responseDto);

        List<ModelResponseDto> response = modelService.getModelsByName("note");

        assertThat(response).containsExactly(responseDto);
        verify(modelRepository).findByNameContainingIgnoreCase("note");
    }

    @Test
    void shouldCompareModelsAndReturnAggregatedMetrics() {
        List<Long> ids = List.of(1L, 2L, 3L);
        Model iphone15 = createModel(1L, "iPhone 15", 5299L, 5);
        Model galaxyS24 = createModel(2L, "Galaxy S24", 4699L, 4);
        Model redmiNote13 = createModel(3L, "Redmi Note 13", 1899L, 4);

        when(modelRepository.findAllByIdIn(ids)).thenReturn(List.of(iphone15, galaxyS24, redmiNote13));
        when(modelMapper.map(iphone15, ModelResponseDto.class)).thenReturn(createResponseDto(iphone15));
        when(modelMapper.map(galaxyS24, ModelResponseDto.class)).thenReturn(createResponseDto(galaxyS24));
        when(modelMapper.map(redmiNote13, ModelResponseDto.class)).thenReturn(createResponseDto(redmiNote13));

        ModelComparisonResponse response = modelService.compareModels(ids);

        assertThat(response.getModels()).hasSize(3);
        assertThat(response.getModels())
                .extracting(ModelResponseDto::getName)
                .containsExactly("iPhone 15", "Galaxy S24", "Redmi Note 13");
        assertThat(response.getLowestPrice()).isEqualByComparingTo("1899");
        assertThat(response.getHighestPrice()).isEqualByComparingTo("5299");
        assertThat(response.getAveragePrice()).isEqualByComparingTo("3965.67");
        assertThat(response.getLowestRating()).isEqualTo(4);
        assertThat(response.getHighestRating()).isEqualTo(5);
        assertThat(response.getAverageRating()).isEqualTo(4.333333333333333);

        verify(modelRepository).findAllByIdIn(ids);
    }

    @Test
    void shouldThrowWhenCompareModelsHasMissingIds() {
        List<Long> ids = List.of(1L, 2L);
        Model iphone15 = createModel(1L, "iPhone 15", 5299L, 5);

        when(modelRepository.findAllByIdIn(ids)).thenReturn(List.of(iphone15));

        assertThatThrownBy(() -> modelService.compareModels(ids))
                .isInstanceOf(NoSuchResourceFoundException.class)
                .hasMessage("One or more informed ids were not found.");

        verify(modelRepository).findAllByIdIn(ids);
    }

    @Test
    void shouldThrowWhenCompareModelsHasEmptyIds() {
        assertThatThrownBy(() -> modelService.compareModels(List.of()))
                .isInstanceOf(BadResourceRequestException.class)
                .hasMessage("ids must not be empty");

        verifyNoInteractions(modelRepository);
    }

    private Model createModel(Long id, String name, long price, int rating) {
        Model model = new Model();
        model.setId(id);
        model.setName(name);
        model.setPrice(BigDecimal.valueOf(price));
        model.setRating(rating);
        return model;
    }

    private CreateModelRequest createRequest(String name, long price, int rating, String specifications) {
        CreateModelRequest request = new CreateModelRequest();
        request.setName(name);
        request.setDescription("Smartphone premium");
        request.setPrice(BigDecimal.valueOf(price));
        request.setRating(rating);
        request.setSpecifications(specifications);
        return request;
    }

    private ModelResponseDto createResponseDto(Model model) {
        ModelResponseDto responseDto = new ModelResponseDto();
        responseDto.setId(model.getId());
        responseDto.setName(model.getName());
        responseDto.setPrice(model.getPrice());
        responseDto.setRating(model.getRating());
        return responseDto;
    }
}
