package com.hackerrank.sample.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackerrank.sample.repository.ModelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductLifecycleIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private ModelRepository repository;

    @Test
    void createReadCompareAndDeleteThroughPublicContract() throws Exception {
        repository.deleteAllInBatch();
        mockMvc.perform(post("/products").contentType("application/json").content("""
                {"name":"Portfolio product","urlImage":"https://example.com/product.png",
                 "description":"A product for comparison","price":19.90,"rating":4,
                 "specifications":"128GB"}
                """))
                .andExpect(status().isCreated()).andExpect(content().string(""));
        var listing = mockMvc.perform(get("/products")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andReturn();
        long id = mapper.readTree(listing.getResponse().getContentAsString()).get(0).get("id").asLong();
        mockMvc.perform(get("/products/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Portfolio product"));
        mockMvc.perform(post("/products/compare").contentType("application/json")
                        .content("{\"ids\":[" + id + "]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.products[0].id").value(id))
                .andExpect(jsonPath("$.models").doesNotExist())
                .andExpect(jsonPath("$.averagePrice").value(19.90));
        mockMvc.perform(delete("/products/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/products/{id}", id)).andExpect(status().isNotFound());
        mockMvc.perform(get("/products")).andExpect(content().json("[]"));
    }
}
