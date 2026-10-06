package com.hackerrank.sample.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class OperationsIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthAndProbesAreAvailableWithoutInternalDetails() throws Exception {
        for (String path : new String[]{"/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness"}) {
            mockMvc.perform(get(path)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"))
                    .andExpect(jsonPath("$.components").doesNotExist());
        }
        mockMvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
    }

    @Test
    void requestIsLoggedWithCorrelationAndWithoutQueryValues(CapturedOutput output) throws Exception {
        var result = mockMvc.perform(get("/products/search").param("name", "sensitive-search-value"))
                .andExpect(status().isOk()).andReturn();
        String id = result.getResponse().getHeader("X-Request-ID");
        assertThat(id).isNotBlank();
        assertThat(output.getOut()).contains(id, "route=/products/search", "status=200", "durationMs=")
                .doesNotContain("sensitive-search-value");
        assertThat(MDC.get("requestId")).isNull();
        var second = mockMvc.perform(get("/products")).andReturn();
        assertThat(second.getResponse().getHeader("X-Request-ID")).isNotEqualTo(id);
    }

    @Test
    void badParametersReturn400() throws Exception {
        mockMvc.perform(get("/products/not-a-number")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/products/search")).andExpect(status().isBadRequest());
    }

    @Test
    void unknownRoutesAndUnsupportedRequestsKeepHttpSemantics() throws Exception {
        mockMvc.perform(get("/does-not-exist")).andExpect(status().isNotFound());
        mockMvc.perform(get("/models")).andExpect(status().isNotFound());
        mockMvc.perform(put("/products")).andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"));
        mockMvc.perform(post("/products").contentType("text/plain").content("invalid"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
