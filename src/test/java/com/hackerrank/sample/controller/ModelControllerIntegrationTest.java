package com.hackerrank.sample.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackerrank.sample.model.Model;
import com.hackerrank.sample.repository.ModelRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ModelControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelRepository modelRepository;

    private Model iphone15;
    private Model galaxyS24;
    private Model redmiNote13;

    @BeforeEach
    void setUp() {
        modelRepository.deleteAllInBatch();
        iphone15 = saveModel(
                "iPhone 15",
                "https://example.com/images/iphone15.png",
                "Smartphone Apple com tela Super Retina XDR e chip A16 Bionic.",
                5299L,
                5,
                "128GB, 6GB RAM, Câmera 48MP");
        galaxyS24 = saveModel(
                "Galaxy S24",
                "https://example.com/images/galaxys24.png",
                "Smartphone Samsung com Galaxy AI e tela AMOLED 120Hz.",
                4699L,
                4,
                "256GB, 8GB RAM, Câmera tripla 50MP");
        redmiNote13 = saveModel(
                "Redmi Note 13",
                "https://example.com/images/redminote13.png",
                "Smartphone Xiaomi com ótimo custo-benefício e bateria duradoura.",
                1899L,
                4,
                "256GB, 8GB RAM, Bateria 5000mAh");
    }

    @Test
    void shouldCreateModelAndReturnCreated() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "name", "Pixel 8",
                "urlImage", "https://example.com/images/pixel8.png",
                "description", "Smartphone Google com Android puro e câmera avançada.",
                "price", 3999L,
                "rating", 5,
                "specifications", "128GB, 8GB RAM, Tensor G3"));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        List<Model> savedModels = modelRepository.findByNameContainingIgnoreCase("Pixel 8");

        assertThat(savedModels).hasSize(1);
        assertThat(savedModels.get(0).getId()).isNotNull();
        assertThat(savedModels.get(0).getDescription())
                .isEqualTo("Smartphone Google com Android puro e câmera avançada.");
        assertThat(savedModels.get(0).getPrice()).isEqualByComparingTo("3999");
        assertThat(savedModels.get(0).getRating()).isEqualTo(5);
        assertThat(savedModels.get(0).getSpecifications()).isEqualTo("128GB, 8GB RAM, Tensor G3");
    }

    @Test
    void shouldReturnBadRequestWhenCreateModelBodyIsInvalid() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "name", " ",
                "urlImage", "https://example.com/images/invalid.png",
                "description", " ",
                "price", 0L,
                "rating", 6,
                "specifications", " "));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("name: name must not be blank")))
                .andExpect(jsonPath("$.message", containsString("description: description must not be blank")))
                .andExpect(jsonPath("$.message", containsString("price: price must be greater than 0")))
                .andExpect(jsonPath("$.message", containsString("rating: rating must be between 1 and 5")))
                .andExpect(jsonPath("$.message", containsString("specifications: specifications must not be blank")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnBadRequestWhenCreateModelBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request body."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnAllModels() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("iPhone 15", "Galaxy S24", "Redmi Note 13")));
    }

    @Test
    void shouldReturnModelById() throws Exception {
        mockMvc.perform(get("/products/{id}", iphone15.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(iphone15.getId()))
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.urlImage").value("https://example.com/images/iphone15.png"))
                .andExpect(jsonPath("$.description")
                        .value("Smartphone Apple com tela Super Retina XDR e chip A16 Bionic."))
                .andExpect(jsonPath("$.price").value(5299))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void shouldReturnNotFoundWhenModelDoesNotExist() throws Exception {
        mockMvc.perform(get("/products/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No product with given id found."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldSearchModelsByPartialNameIgnoringCase() throws Exception {
        mockMvc.perform(get("/products/search")
                        .param("name", "note"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].name", contains("Redmi Note 13")));
    }

    @Test
    void shouldCompareModelsAndReturnAggregatedMetrics() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "ids", List.of(iphone15.getId(), galaxyS24.getId(), redmiNote13.getId())));

        mockMvc.perform(post("/products/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products", hasSize(3)))
                .andExpect(jsonPath("$.products[*].name", containsInAnyOrder("iPhone 15", "Galaxy S24", "Redmi Note 13")))
                .andExpect(jsonPath("$.lowestPrice").value(1899))
                .andExpect(jsonPath("$.highestPrice").value(5299))
                .andExpect(jsonPath("$.averagePrice").value(closeTo(3965.67, 0.0001)))
                .andExpect(jsonPath("$.lowestRating").value(4))
                .andExpect(jsonPath("$.highestRating").value(5))
                .andExpect(jsonPath("$.averageRating").value(closeTo(4.333333333333333, 0.0001)));
    }

    @Test
    void shouldReturnBadRequestWhenCompareRequestHasEmptyIds() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("ids", List.of()));

        mockMvc.perform(post("/products/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("ids: ids must not be empty")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnBadRequestWhenCompareRequestContainsNullIds() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("ids", Arrays.asList(iphone15.getId(), null)));

        mockMvc.perform(post("/products/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("ids")))
                .andExpect(jsonPath("$.message", containsString("ids must not contain null values")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnNotFoundWhenComparingUnknownIds() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "ids", List.of(iphone15.getId(), 999999L)));

        mockMvc.perform(post("/products/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("One or more informed ids were not found."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldDeleteModelById() throws Exception {
        mockMvc.perform(delete("/products/{id}", iphone15.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(modelRepository.findById(iphone15.getId())).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownId() throws Exception {
        mockMvc.perform(delete("/products/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No resource found for the provided id."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldDeleteAllModels() throws Exception {
        mockMvc.perform(delete("/products"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(modelRepository.count()).isZero();
    }

    private Model saveModel(String name, String urlImage, String description, long price, int rating, String specifications) {
        Model model = new Model();
        model.setName(name);
        model.setUrlImage(urlImage);
        model.setDescription(description);
        model.setPrice(BigDecimal.valueOf(price));
        model.setRating(rating);
        model.setSpecifications(specifications);
        return modelRepository.save(model);
    }
}
